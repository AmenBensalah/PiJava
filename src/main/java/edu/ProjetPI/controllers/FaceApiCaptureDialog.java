package edu.ProjetPI.controllers;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtLoggingLevel;
import ai.onnxruntime.OrtSession;
import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class FaceApiCaptureDialog {
    private static final int PREVIEW_WIDTH = 760;
    private static final int PREVIEW_HEIGHT = 520;

    private static final int DETECTOR_INPUT_SIZE = 640;
    private static final int RECOGNIZER_INPUT_SIZE = 112;
    private static final int[] STRIDES = new int[]{8, 16, 32};
    private static final float DETECTION_SCORE_THRESHOLD = 0.75f;
    private static final float DETECTION_NMS_THRESHOLD = 0.3f;

    private static final String DETECTOR_MODEL_NAME = "face_detection_yunet_2023mar.onnx";
    private static final String RECOGNIZER_MODEL_NAME = "face_recognition_sface_2021dec.onnx";
    private static final String DETECTOR_MODEL_URL = "https://huggingface.co/opencv/face_detection_yunet/resolve/main/face_detection_yunet_2023mar.onnx?download=true";
    private static final String RECOGNIZER_MODEL_URL = "https://huggingface.co/opencv/face_recognition_sface/resolve/main/face_recognition_sface_2021dec.onnx?download=true";

    private static final double[][] ALIGN_TEMPLATE = new double[][]{
            {38.2946, 51.6963},
            {73.5318, 51.5014},
            {56.0252, 71.7366},
            {41.5493, 92.3655},
            {70.7299, 92.2041}
    };

    private static volatile FacePipeline pipeline;

    private FaceApiCaptureDialog() {
    }

    public static Optional<List<Double>> captureDescriptor(Window owner) {
        FacePipeline activePipeline = getPipeline();

        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new IllegalStateException("No webcam found on this machine.");
        }
        webcam.setViewSize(pickPreferredSize(webcam.getViewSizes()));
        webcam.open();

        Dialog<List<Double>> dialog = new Dialog<>();
        dialog.setTitle("Face ID Capture");
        // dialog.setHeaderText removed
        if (owner != null) {
            dialog.initOwner(owner);
        }
        ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(cancelType);
        dialog.setResultConverter(button -> null);

        ImageView preview = new ImageView();
        preview.setFitWidth(PREVIEW_WIDTH);
        preview.setFitHeight(PREVIEW_HEIGHT);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);

        StackPane previewShell = new StackPane(preview);
        previewShell.getStyleClass().add("face-preview-shell");

        Button captureButton = new Button("Capture");
        captureButton.setMaxWidth(Double.MAX_VALUE);
        captureButton.getStyleClass().add("face-capture-button");

        Label status = new Label("Starting camera...");
        status.setWrapText(true);
        status.getStyleClass().add("face-dialog-status");

        VBox content = new VBox(12, previewShell, captureButton, status);
        content.setPadding(new Insets(6, 0, 0, 0));
        content.getStyleClass().add("face-dialog-content");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStyleClass().add("face-dialog-pane");
        var faceDialogCss = FaceApiCaptureDialog.class.getResource("/edu/ProjetPI/views/esportify/face-capture.css");
        if (faceDialogCss != null) {
            dialog.getDialogPane().getStylesheets().add(faceDialogCss.toExternalForm());
        }
        var cancelNode = dialog.getDialogPane().lookupButton(cancelType);
        if (cancelNode != null) {
            cancelNode.getStyleClass().add("face-cancel-button");
        }

        AtomicReference<BufferedImage> latestFrame = new AtomicReference<>();
        AtomicBoolean captureInProgress = new AtomicBoolean(false);
        AtomicBoolean cameraReadyShown = new AtomicBoolean(false);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ExecutorService worker = Executors.newSingleThreadExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                BufferedImage image = webcam.getImage();
                if (image == null) {
                    return;
                }
                latestFrame.set(copyImage(image));
                Platform.runLater(() -> {
                    preview.setImage(SwingFXUtils.toFXImage(image, null));
                    if (!captureInProgress.get() && !cameraReadyShown.get()) {
                        status.setText("Camera ready. Keep one face centered and click Capture.");
                        cameraReadyShown.set(true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> status.setText("Camera stream error: " + e.getMessage()));
            }
        }, 0, 40, TimeUnit.MILLISECONDS);

        captureButton.setOnAction(event -> {
            captureButton.setDisable(true);
            captureInProgress.set(true);
            status.setText("Analyzing face... stay still for a second.");

            CompletableFuture.supplyAsync(() -> captureStableDescriptor(latestFrame, activePipeline), worker)
                    .orTimeout(12, TimeUnit.SECONDS)
                    .whenComplete((descriptor, error) -> Platform.runLater(() -> {
                        captureButton.setDisable(false);
                        captureInProgress.set(false);
                        if (error != null) {
                            if (error instanceof TimeoutException || error.getCause() instanceof TimeoutException) {
                                status.setText("Capture timed out. Keep your face centered and try again.");
                            } else {
                                status.setText("Capture failed: " + unwrapMessage(error));
                            }
                            return;
                        }
                        status.setText("Face descriptor captured successfully.");
                        dialog.setResult(descriptor);
                        dialog.close();
                    }));
        });

        try {
            return dialog.showAndWait();
        } finally {
            scheduler.shutdownNow();
            worker.shutdownNow();
            if (webcam.isOpen()) {
                webcam.close();
            }
        }
    }

    private static List<Double> captureStableDescriptor(AtomicReference<BufferedImage> latestFrame, FacePipeline activePipeline) {
        List<double[]> samples = new ArrayList<>();
        String lastError = "No frame received from camera.";

        for (int i = 0; i < 20; i++) {
            BufferedImage frame = latestFrame.get();
            if (frame == null) {
                sleep(100);
                continue;
            }

            try {
                samples.add(activePipeline.extractDescriptor(frame));
                if (samples.size() >= 3) {
                    break;
                }
            } catch (Exception e) {
                lastError = e.getMessage();
            }
            sleep(100);
        }

        if (samples.size() < 3) {
            throw new IllegalStateException(lastError);
        }

        int featureLength = samples.get(0).length;
        if (featureLength == 0) {
            throw new IllegalStateException("Face descriptor is empty.");
        }
        for (int s = 1; s < samples.size(); s++) {
            if (samples.get(s).length != featureLength) {
                throw new IllegalStateException("Inconsistent descriptor length between captures.");
            }
        }

        double[] avg = new double[featureLength];
        for (double[] sample : samples) {
            for (int i = 0; i < featureLength; i++) {
                avg[i] += sample[i];
            }
        }
        for (int i = 0; i < featureLength; i++) {
            avg[i] /= samples.size();
        }
        avg = normalizeL2(avg);

        List<Double> descriptor = new ArrayList<>(featureLength);
        for (double v : avg) {
            descriptor.add(v);
        }
        return descriptor;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String unwrapMessage(Throwable error) {
        Throwable cursor = error;
        while (cursor.getCause() != null) {
            cursor = cursor.getCause();
        }
        return cursor.getMessage() == null ? "Unknown error." : cursor.getMessage();
    }

    private static Dimension pickPreferredSize(Dimension[] sizes) {
        if (sizes == null || sizes.length == 0) {
            return new Dimension(640, 480);
        }
        return List.of(sizes).stream()
                .max(Comparator.comparingInt(d -> d.width * d.height))
                .orElse(new Dimension(640, 480));
    }

    private static FacePipeline getPipeline() {
        FacePipeline cached = pipeline;
        if (cached != null) {
            return cached;
        }
        synchronized (FaceApiCaptureDialog.class) {
            if (pipeline == null) {
                Path detectorModel = resolveModelPath(DETECTOR_MODEL_NAME, DETECTOR_MODEL_URL, 200_000);
                Path recognizerModel = resolveModelPath(RECOGNIZER_MODEL_NAME, RECOGNIZER_MODEL_URL, 1_000_000);
                pipeline = FacePipeline.load(detectorModel, recognizerModel);
            }
            return pipeline;
        }
    }

    private static Path resolveModelPath(String modelName, String url, long minSize) {
        try {
            Path modelDir = Path.of(System.getProperty("user.home"), ".pijava-faceid", "models");
            Files.createDirectories(modelDir);
            Path modelPath = modelDir.resolve(modelName);
            if (Files.exists(modelPath) && Files.size(modelPath) >= minSize) {
                return modelPath;
            }

            Path tmp = modelPath.resolveSibling(modelName + ".tmp");
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMinutes(3))
                    .GET()
                    .build();
            HttpResponse<Path> response = client.send(req, HttpResponse.BodyHandlers.ofFile(tmp));
            if (response.statusCode() < 200 || response.statusCode() >= 300 || Files.size(tmp) < minSize) {
                throw new IOException("HTTP " + response.statusCode());
            }
            Files.move(tmp, modelPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return modelPath;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to prepare ONNX model '" + modelName + "': " + e.getMessage(), e);
        }
    }

    private static BufferedImage copyImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = copy.createGraphics();
        try {
            g.drawImage(src, 0, 0, null);
            return copy;
        } finally {
            g.dispose();
        }
    }

    private static BufferedImage resize(BufferedImage source, int targetW, int targetH) {
        BufferedImage out = new BufferedImage(targetW, targetH, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(source, 0, 0, targetW, targetH, null);
            return out;
        } finally {
            g.dispose();
        }
    }

    private static double[] normalizeL2(double[] input) {
        double norm = 0d;
        for (double v : input) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (!Double.isFinite(norm) || norm < 1e-12) {
            throw new IllegalStateException("Face descriptor is invalid.");
        }
        double[] out = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = input[i] / norm;
        }
        return out;
    }

    private static final class FacePipeline {
        private final FaceDetectionModel detector;
        private final FaceEmbeddingModel recognizer;

        private FacePipeline(FaceDetectionModel detector, FaceEmbeddingModel recognizer) {
            this.detector = detector;
            this.recognizer = recognizer;
        }

        static FacePipeline load(Path detectorModel, Path recognizerModel) {
            OrtEnvironment env = OrtEnvironment.getEnvironment(OrtLoggingLevel.ORT_LOGGING_LEVEL_ERROR, "pijava-faceid");
            return new FacePipeline(
                    FaceDetectionModel.load(env, detectorModel),
                    FaceEmbeddingModel.load(env, recognizerModel)
            );
        }

        double[] extractDescriptor(BufferedImage frame) {
            List<FaceDetection> faces = detector.detect(frame);
            if (faces.isEmpty()) {
                throw new IllegalStateException("No face detected. Keep your face centered and well lit.");
            }
            if (faces.size() > 1) {
                throw new IllegalStateException("Multiple faces detected. Only one face should be visible.");
            }

            FaceDetection face = faces.get(0);
            double frameArea = frame.getWidth() * frame.getHeight();
            if (face.w * face.h < frameArea * 0.025) {
                throw new IllegalStateException("Face too far from camera. Move closer.");
            }

            BufferedImage aligned = alignFace(frame, face.landmarks);
            double brightness = estimateBrightness(aligned);
            if (brightness < 30 || brightness > 235) {
                throw new IllegalStateException("Lighting is not good. Avoid very dark or overexposed lighting.");
            }
            double sharpness = estimateSharpness(aligned);
            if (sharpness < 25) {
                throw new IllegalStateException("Image is blurry. Hold still and improve camera focus.");
            }

            return recognizer.extractDescriptor(aligned);
        }

        private static BufferedImage alignFace(BufferedImage image, double[][] landmarks) {
            double[] m = solveAffineLeastSquares(landmarks, ALIGN_TEMPLATE);
            if (m == null) {
                throw new IllegalStateException("Unable to align detected face.");
            }
            AffineTransform transform = new AffineTransform(m[0], m[3], m[1], m[4], m[2], m[5]);
            BufferedImage src = image.getType() == BufferedImage.TYPE_3BYTE_BGR ? image : copyImage(image);
            BufferedImage aligned = new BufferedImage(RECOGNIZER_INPUT_SIZE, RECOGNIZER_INPUT_SIZE, BufferedImage.TYPE_3BYTE_BGR);
            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
            op.filter(src, aligned);
            return aligned;
        }

        private static double estimateBrightness(BufferedImage image) {
            double sum = 0d;
            int w = image.getWidth();
            int h = image.getHeight();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    sum += 0.299 * r + 0.587 * g + 0.114 * b;
                }
            }
            return sum / (w * h);
        }

        private static double estimateSharpness(BufferedImage image) {
            int w = image.getWidth();
            int h = image.getHeight();
            if (w < 3 || h < 3) {
                return 0;
            }
            double[] lap = new double[(w - 2) * (h - 2)];
            int idx = 0;
            for (int y = 1; y < h - 1; y++) {
                for (int x = 1; x < w - 1; x++) {
                    double c = gray(image.getRGB(x, y));
                    double l = gray(image.getRGB(x - 1, y));
                    double r = gray(image.getRGB(x + 1, y));
                    double u = gray(image.getRGB(x, y - 1));
                    double d = gray(image.getRGB(x, y + 1));
                    lap[idx++] = -4 * c + l + r + u + d;
                }
            }

            double mean = 0d;
            for (double v : lap) {
                mean += v;
            }
            mean /= lap.length;

            double variance = 0d;
            for (double v : lap) {
                double t = v - mean;
                variance += t * t;
            }
            return variance / lap.length;
        }

        private static double gray(int rgb) {
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            return 0.299 * r + 0.587 * g + 0.114 * b;
        }

        private static double[] solveAffineLeastSquares(double[][] src, double[][] dst) {
            if (src.length != 5 || dst.length != 5) {
                return null;
            }
            double[][] ata = new double[6][6];
            double[] atb = new double[6];

            for (int i = 0; i < 5; i++) {
                double x = src[i][0];
                double y = src[i][1];
                double u = dst[i][0];
                double v = dst[i][1];

                double[] rowU = new double[]{x, y, 1, 0, 0, 0};
                double[] rowV = new double[]{0, 0, 0, x, y, 1};

                accumulateNormalEq(rowU, u, ata, atb);
                accumulateNormalEq(rowV, v, ata, atb);
            }

            return solveLinearSystem(ata, atb);
        }

        private static void accumulateNormalEq(double[] row, double target, double[][] ata, double[] atb) {
            for (int i = 0; i < row.length; i++) {
                atb[i] += row[i] * target;
                for (int j = 0; j < row.length; j++) {
                    ata[i][j] += row[i] * row[j];
                }
            }
        }

        private static double[] solveLinearSystem(double[][] a, double[] b) {
            int n = b.length;
            double[][] aug = new double[n][n + 1];
            for (int i = 0; i < n; i++) {
                System.arraycopy(a[i], 0, aug[i], 0, n);
                aug[i][n] = b[i];
            }

            for (int col = 0; col < n; col++) {
                int pivot = col;
                double best = Math.abs(aug[col][col]);
                for (int r = col + 1; r < n; r++) {
                    double cand = Math.abs(aug[r][col]);
                    if (cand > best) {
                        best = cand;
                        pivot = r;
                    }
                }
                if (best < 1e-12) {
                    return null;
                }
                if (pivot != col) {
                    double[] tmp = aug[col];
                    aug[col] = aug[pivot];
                    aug[pivot] = tmp;
                }

                double div = aug[col][col];
                for (int j = col; j <= n; j++) {
                    aug[col][j] /= div;
                }

                for (int r = 0; r < n; r++) {
                    if (r == col) {
                        continue;
                    }
                    double factor = aug[r][col];
                    if (Math.abs(factor) < 1e-12) {
                        continue;
                    }
                    for (int j = col; j <= n; j++) {
                        aug[r][j] -= factor * aug[col][j];
                    }
                }
            }

            double[] x = new double[n];
            for (int i = 0; i < n; i++) {
                x[i] = aug[i][n];
            }
            return x;
        }
    }

    private static final class FaceDetectionModel {
        private final OrtEnvironment env;
        private final OrtSession session;
        private final String inputName;

        private FaceDetectionModel(OrtEnvironment env, OrtSession session, String inputName) {
            this.env = env;
            this.session = session;
            this.inputName = inputName;
        }

        static FaceDetectionModel load(OrtEnvironment env, Path modelPath) {
            try {
                OrtSession.SessionOptions options = new OrtSession.SessionOptions();
                options.setSessionLogLevel(OrtLoggingLevel.ORT_LOGGING_LEVEL_FATAL);
                OrtSession session = env.createSession(modelPath.toString(), options);
                String inputName = session.getInputNames().stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Face detector model has no input."));
                return new FaceDetectionModel(env, session, inputName);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to load face detector model: " + e.getMessage(), e);
            }
        }

        List<FaceDetection> detect(BufferedImage frame) {
            BufferedImage resized = resize(frame, DETECTOR_INPUT_SIZE, DETECTOR_INPUT_SIZE);
            float[] input = toNchwBgr(resized);

            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(input), new long[]{1, 3, DETECTOR_INPUT_SIZE, DETECTOR_INPUT_SIZE});
                 OrtSession.Result result = session.run(Map.of(inputName, inputTensor))) {

                float[][] cls8 = tensor2d(result, "cls_8");
                float[][] cls16 = tensor2d(result, "cls_16");
                float[][] cls32 = tensor2d(result, "cls_32");
                float[][] obj8 = tensor2d(result, "obj_8");
                float[][] obj16 = tensor2d(result, "obj_16");
                float[][] obj32 = tensor2d(result, "obj_32");
                float[][] bbox8 = tensor2d(result, "bbox_8");
                float[][] bbox16 = tensor2d(result, "bbox_16");
                float[][] bbox32 = tensor2d(result, "bbox_32");
                float[][] kps8 = tensor2d(result, "kps_8");
                float[][] kps16 = tensor2d(result, "kps_16");
                float[][] kps32 = tensor2d(result, "kps_32");

                List<FaceDetection> all = new ArrayList<>();
                decodeStride(all, STRIDES[0], cls8, obj8, bbox8, kps8);
                decodeStride(all, STRIDES[1], cls16, obj16, bbox16, kps16);
                decodeStride(all, STRIDES[2], cls32, obj32, bbox32, kps32);

                List<FaceDetection> kept = nms(all, DETECTION_NMS_THRESHOLD);
                scaleDetectionsToOriginal(kept, frame.getWidth(), frame.getHeight());
                kept.sort((a, b) -> Float.compare(b.score, a.score));
                return kept;
            } catch (OrtException e) {
                throw new IllegalStateException("Face detection inference failed: " + e.getMessage(), e);
            }
        }

        @SuppressWarnings("unchecked")
        private static float[][] tensor2d(OrtSession.Result result, String name) throws OrtException {
            OnnxValue val = result.get(name).orElseThrow(() -> new IllegalStateException("Missing output tensor: " + name));
            Object out = val.getValue();
            if (!(out instanceof float[][][] tensor3d) || tensor3d.length == 0) {
                throw new IllegalStateException("Unexpected tensor shape for " + name + ".");
            }
            return tensor3d[0];
        }

        private static void decodeStride(List<FaceDetection> out, int stride, float[][] cls, float[][] obj, float[][] bbox, float[][] kps) {
            int cols = DETECTOR_INPUT_SIZE / stride;
            int rows = DETECTOR_INPUT_SIZE / stride;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int idx = r * cols + c;
                    float clsScore = clamp01(cls[idx][0]);
                    float objScore = clamp01(obj[idx][0]);
                    float score = (float) Math.sqrt(clsScore * objScore);
                    if (score < DETECTION_SCORE_THRESHOLD) {
                        continue;
                    }

                    float cx = (c + bbox[idx][0]) * stride;
                    float cy = (r + bbox[idx][1]) * stride;
                    float w = (float) (Math.exp(bbox[idx][2]) * stride);
                    float h = (float) (Math.exp(bbox[idx][3]) * stride);
                    float x = cx - w / 2f;
                    float y = cy - h / 2f;

                    double[][] landmarks = new double[5][2];
                    for (int n = 0; n < 5; n++) {
                        landmarks[n][0] = (kps[idx][2 * n] + c) * stride;
                        landmarks[n][1] = (kps[idx][2 * n + 1] + r) * stride;
                    }

                    out.add(new FaceDetection(x, y, w, h, score, landmarks));
                }
            }
        }

        private static float clamp01(float v) {
            if (v < 0f) {
                return 0f;
            }
            if (v > 1f) {
                return 1f;
            }
            return v;
        }

        private static List<FaceDetection> nms(List<FaceDetection> detections, float iouThreshold) {
            if (detections.isEmpty()) {
                return detections;
            }
            detections.sort((a, b) -> Float.compare(b.score, a.score));
            List<FaceDetection> kept = new ArrayList<>();
            for (FaceDetection candidate : detections) {
                boolean suppressed = false;
                for (FaceDetection selected : kept) {
                    if (iou(candidate, selected) > iouThreshold) {
                        suppressed = true;
                        break;
                    }
                }
                if (!suppressed) {
                    kept.add(candidate);
                }
            }
            return kept;
        }

        private static float iou(FaceDetection a, FaceDetection b) {
            float ax2 = a.x + a.w;
            float ay2 = a.y + a.h;
            float bx2 = b.x + b.w;
            float by2 = b.y + b.h;

            float interX1 = Math.max(a.x, b.x);
            float interY1 = Math.max(a.y, b.y);
            float interX2 = Math.min(ax2, bx2);
            float interY2 = Math.min(ay2, by2);
            float interW = Math.max(0f, interX2 - interX1);
            float interH = Math.max(0f, interY2 - interY1);
            float inter = interW * interH;
            float union = a.w * a.h + b.w * b.h - inter;
            if (union <= 0f) {
                return 0f;
            }
            return inter / union;
        }

        private static void scaleDetectionsToOriginal(List<FaceDetection> detections, int w, int h) {
            double sx = (double) w / DETECTOR_INPUT_SIZE;
            double sy = (double) h / DETECTOR_INPUT_SIZE;

            for (FaceDetection d : detections) {
                d.x = (float) (d.x * sx);
                d.y = (float) (d.y * sy);
                d.w = (float) (d.w * sx);
                d.h = (float) (d.h * sy);

                d.x = clamp(d.x, 0f, w - 1f);
                d.y = clamp(d.y, 0f, h - 1f);
                d.w = clamp(d.w, 1f, w - d.x);
                d.h = clamp(d.h, 1f, h - d.y);

                for (int i = 0; i < 5; i++) {
                    d.landmarks[i][0] = clamp((float) (d.landmarks[i][0] * sx), 0f, w - 1f);
                    d.landmarks[i][1] = clamp((float) (d.landmarks[i][1] * sy), 0f, h - 1f);
                }
            }
        }

        private static float clamp(float v, float min, float max) {
            if (v < min) {
                return min;
            }
            if (v > max) {
                return max;
            }
            return v;
        }

        private static float[] toNchwBgr(BufferedImage image) {
            int w = image.getWidth();
            int h = image.getHeight();
            float[] out = new float[3 * w * h];
            int stride = w * h;
            int idx = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = image.getRGB(x, y);
                    float r = (rgb >> 16) & 0xFF;
                    float g = (rgb >> 8) & 0xFF;
                    float b = rgb & 0xFF;
                    out[idx] = b;
                    out[idx + stride] = g;
                    out[idx + (2 * stride)] = r;
                    idx++;
                }
            }
            return out;
        }
    }

    private static final class FaceEmbeddingModel {
        private final OrtEnvironment env;
        private final OrtSession session;
        private final String inputName;

        private FaceEmbeddingModel(OrtEnvironment env, OrtSession session, String inputName) {
            this.env = env;
            this.session = session;
            this.inputName = inputName;
        }

        static FaceEmbeddingModel load(OrtEnvironment env, Path modelPath) {
            try {
                OrtSession.SessionOptions options = new OrtSession.SessionOptions();
                options.setSessionLogLevel(OrtLoggingLevel.ORT_LOGGING_LEVEL_FATAL);
                OrtSession session = env.createSession(modelPath.toString(), options);
                String inputName = session.getInputNames().stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Face recognizer model has no input."));
                return new FaceEmbeddingModel(env, session, inputName);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to load face recognizer model: " + e.getMessage(), e);
            }
        }

        double[] extractDescriptor(BufferedImage alignedFace) {
            BufferedImage face = alignedFace;
            if (face.getWidth() != RECOGNIZER_INPUT_SIZE || face.getHeight() != RECOGNIZER_INPUT_SIZE) {
                face = resize(face, RECOGNIZER_INPUT_SIZE, RECOGNIZER_INPUT_SIZE);
            }
            float[] input = toNchwRgb(face);

            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(input), new long[]{1, 3, RECOGNIZER_INPUT_SIZE, RECOGNIZER_INPUT_SIZE});
                 OrtSession.Result result = session.run(Map.of(inputName, inputTensor))) {
                Object output = result.get(0).getValue();
                double[] feature = flattenOutput(output);
                if (feature.length < 32) {
                    throw new IllegalStateException("Unexpected descriptor length " + feature.length + ".");
                }
                return normalizeL2(feature);
            } catch (OrtException e) {
                throw new IllegalStateException("Face embedding inference failed: " + e.getMessage(), e);
            }
        }

        private static float[] toNchwRgb(BufferedImage image) {
            int w = image.getWidth();
            int h = image.getHeight();
            float[] out = new float[3 * w * h];
            int stride = w * h;
            int idx = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = image.getRGB(x, y);
                    float r = (rgb >> 16) & 0xFF;
                    float g = (rgb >> 8) & 0xFF;
                    float b = rgb & 0xFF;
                    out[idx] = r;
                    out[idx + stride] = g;
                    out[idx + (2 * stride)] = b;
                    idx++;
                }
            }
            return out;
        }

        private static double[] flattenOutput(Object output) {
            if (output instanceof float[][] matrix && matrix.length > 0) {
                float[] row = matrix[0];
                double[] out = new double[row.length];
                for (int i = 0; i < row.length; i++) {
                    out[i] = row[i];
                }
                return out;
            }
            if (output instanceof float[] vector) {
                double[] out = new double[vector.length];
                for (int i = 0; i < vector.length; i++) {
                    out[i] = vector[i];
                }
                return out;
            }
            throw new IllegalStateException("Unsupported recognizer output type: " + output.getClass().getName());
        }
    }

    private static final class FaceDetection {
        private float x;
        private float y;
        private float w;
        private float h;
        private final float score;
        private final double[][] landmarks;

        private FaceDetection(float x, float y, float w, float h, float score, double[][] landmarks) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.score = score;
            this.landmarks = landmarks;
        }
    }
}
