package edu.ProjetPI.services;

import edu.ProjetPI.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FaceIdAuthService {
    public static final int DESCRIPTOR_LENGTH = 512;
    public static final int LEGACY_DESCRIPTOR_LENGTH = 128;
    public static final double L2_MATCH_THRESHOLD = 1.128d;
    public static final double COSINE_MATCH_THRESHOLD = 0.363d;

    private final UserService userService;

    public FaceIdAuthService(UserService userService) {
        this.userService = userService;
    }

    public double[] normalizeAndValidate(List<Double> descriptor) {
        if (descriptor == null) {
            throw new IllegalArgumentException("Face descriptor is required.");
        }
        if (descriptor.isEmpty()) {
            throw new IllegalArgumentException("Face descriptor is empty.");
        }
        double[] out = new double[descriptor.size()];
        for (int i = 0; i < descriptor.size(); i++) {
            Double value = descriptor.get(i);
            if (value == null || !Double.isFinite(value)) {
                throw new IllegalArgumentException("Face descriptor contains invalid value at index " + i + ".");
            }
            out[i] = value;
        }
        return normalizeL2(resampleIfNeeded(out));
    }

    public String toJson(double[] descriptor) {
        if (descriptor == null || descriptor.length != DESCRIPTOR_LENGTH) {
            throw new IllegalArgumentException("Face descriptor must contain exactly " + DESCRIPTOR_LENGTH + " values.");
        }
        StringBuilder sb = new StringBuilder(2048);
        sb.append('[');
        for (int i = 0; i < descriptor.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(Double.toString(descriptor[i]));
        }
        sb.append(']');
        return sb.toString();
    }

    public double[] parseJson(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Stored face descriptor is empty.");
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new IllegalArgumentException("Stored face descriptor is not a JSON array.");
        }
        String body = trimmed.substring(1, trimmed.length() - 1).trim();
        if (body.isEmpty()) {
            throw new IllegalArgumentException("Stored face descriptor is empty.");
        }
        String[] parts = body.split(",");
        if (parts.length < 32) {
            throw new IllegalArgumentException("Stored face descriptor length is too small.");
        }
        double[] out = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                double value = Double.parseDouble(parts[i].trim());
                if (!Double.isFinite(value)) {
                    throw new IllegalArgumentException("Stored face descriptor has non-finite value at index " + i + ".");
                }
                out[i] = value;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Stored face descriptor has invalid value at index " + i + ".");
            }
        }
        return normalizeL2(resampleIfNeeded(out));
    }

    public double euclideanDistance(double[] a, double[] b) {
        if (a.length != DESCRIPTOR_LENGTH || b.length != DESCRIPTOR_LENGTH) {
            throw new IllegalArgumentException("Face descriptors must be normalized to length " + DESCRIPTOR_LENGTH + ".");
        }
        double sum = 0d;
        for (int i = 0; i < DESCRIPTOR_LENGTH; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    public double cosineSimilarity(double[] a, double[] b) {
        if (a.length != DESCRIPTOR_LENGTH || b.length != DESCRIPTOR_LENGTH) {
            throw new IllegalArgumentException("Face descriptors must be normalized to length " + DESCRIPTOR_LENGTH + ".");
        }
        double dot = 0d;
        for (int i = 0; i < DESCRIPTOR_LENGTH; i++) {
            dot += a[i] * b[i];
        }
        return dot;
    }

    public Optional<FaceMatchResult> authenticateByDescriptor(List<Double> descriptor) {
        double[] probe = normalizeAndValidate(descriptor);
        List<User> users = userService.findUsersWithFaceDescriptor();
        if (users.isEmpty()) {
            return Optional.empty();
        }

        FaceMatchResult best = null;
        for (User user : users) {
            try {
                double[] stored = parseJson(user.getFaceDescriptorJson());
                double distance = euclideanDistance(probe, stored);
                double cosine = cosineSimilarity(probe, stored);
                if (best == null || cosine > best.cosine()) {
                    best = new FaceMatchResult(user, distance, cosine);
                }
            } catch (IllegalArgumentException ignored) {
                // Skip malformed stored descriptor rows.
            }
        }

        if (best == null) {
            return Optional.empty();
        }
        if (best.distance() <= L2_MATCH_THRESHOLD || best.cosine() >= COSINE_MATCH_THRESHOLD) {
            return Optional.of(best);
        }
        return Optional.empty();
    }

    private static double[] resampleIfNeeded(double[] descriptor) {
        if (descriptor.length == DESCRIPTOR_LENGTH) {
            return descriptor;
        }
        if (descriptor.length == LEGACY_DESCRIPTOR_LENGTH) {
            return resample(descriptor, DESCRIPTOR_LENGTH);
        }
        return resample(descriptor, DESCRIPTOR_LENGTH);
    }

    private static double[] resample(double[] input, int targetLength) {
        double[] out = new double[targetLength];
        for (int i = 0; i < targetLength; i++) {
            int start = (int) Math.floor((double) i * input.length / targetLength);
            int end = (int) Math.floor((double) (i + 1) * input.length / targetLength);
            if (end <= start) {
                end = Math.min(start + 1, input.length);
            }
            double sum = 0d;
            for (int j = start; j < end; j++) {
                sum += input[j];
            }
            out[i] = sum / (end - start);
        }
        return out;
    }

    private static double[] normalizeL2(double[] input) {
        double norm = 0d;
        for (double v : input) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (!Double.isFinite(norm) || norm < 1e-12) {
            throw new IllegalArgumentException("Face descriptor norm is invalid.");
        }
        double[] out = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = input[i] / norm;
        }
        return out;
    }

    public record FaceMatchResult(User user, double distance, double cosine) {
    }
}
