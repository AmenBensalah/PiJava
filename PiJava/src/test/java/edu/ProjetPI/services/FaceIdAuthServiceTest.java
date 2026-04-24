package edu.ProjetPI.services;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FaceIdAuthServiceTest {

    private final FaceIdAuthService service = new FaceIdAuthService(null);

    @Test
    void normalizeAndValidateAccepts512AndReturnsUnitNorm() {
        List<Double> descriptor = new ArrayList<>(FaceIdAuthService.DESCRIPTOR_LENGTH);
        for (int i = 0; i < FaceIdAuthService.DESCRIPTOR_LENGTH; i++) {
            descriptor.add((double) (i + 1));
        }

        double[] normalized = service.normalizeAndValidate(descriptor);
        assertEquals(FaceIdAuthService.DESCRIPTOR_LENGTH, normalized.length);

        double norm = 0d;
        for (double v : normalized) {
            norm += v * v;
        }
        assertTrue(Math.abs(Math.sqrt(norm) - 1d) < 1e-9);
    }

    @Test
    void parseJsonAcceptsLegacy128AndResamplesTo512() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < FaceIdAuthService.LEGACY_DESCRIPTOR_LENGTH; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i + 1);
        }
        sb.append(']');

        double[] parsed = service.parseJson(sb.toString());
        assertEquals(FaceIdAuthService.DESCRIPTOR_LENGTH, parsed.length);
    }

    @Test
    void parseJsonRejectsTooShortDescriptor() {
        assertThrows(IllegalArgumentException.class, () -> service.parseJson("[1,2,3]"));
    }

    @Test
    void cosineAndDistanceBehaveAsExpectedOnNormalizedVectors() {
        double[] a = new double[FaceIdAuthService.DESCRIPTOR_LENGTH];
        double[] b = new double[FaceIdAuthService.DESCRIPTOR_LENGTH];
        a[0] = 1d;
        b[0] = 1d;

        assertEquals(0d, service.euclideanDistance(a, b), 1e-9);
        assertEquals(1d, service.cosineSimilarity(a, b), 1e-9);
    }
}
