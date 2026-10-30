package com.brazcubas.apsii.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de borda e de caminho triste do QualityClassifier.
 *
 * Limiares atuais (QualityRules): RTT 60 ms = MODERATE, 150 ms = UNSTABLE;
 * perda 1.0% = MODERATE, 5.0% = UNSTABLE. Todos inclusivos (>=).
 */
class QualityClassifierBoundaryTest {
    private final QualityClassifier classifier = new QualityClassifier();

    // ---------- Bordas de limiar ----------

    @Test
    void rttExactlyAtModerateThresholdIsModerate() {
        // Arrange
        double rtt = 60.0;
        double packetLoss = 0.0;

        // Act
        String quality = classifier.classify(rtt, packetLoss);

        // Assert
        assertEquals("MODERATE", quality);
    }

    @Test
    void rttJustBelowModerateThresholdIsGood() {
        double rtt = 59.99;
        double packetLoss = 0.0;

        String quality = classifier.classify(rtt, packetLoss);

        assertEquals("GOOD", quality);
    }

    @Test
    void rttExactlyAtUnstableThresholdIsUnstable() {
        double rtt = 150.0;
        double packetLoss = 0.0;

        String quality = classifier.classify(rtt, packetLoss);

        assertEquals("UNSTABLE", quality);
    }

    @Test
    void packetLossExactlyAtModerateThresholdIsModerate() {
        double rtt = 0.0;
        double packetLoss = 1.0;

        String quality = classifier.classify(rtt, packetLoss);

        assertEquals("MODERATE", quality);
    }

    @Test
    void packetLossJustBelowModerateThresholdIsGood() {
        double rtt = 0.0;
        double packetLoss = 0.99;

        String quality = classifier.classify(rtt, packetLoss);

        assertEquals("GOOD", quality);
    }

    // ---------- Valores extremos e score ----------

    @Test
    void perfectConnectionScoresOneHundred() {
        int score = classifier.score(0.0, 0.0);

        assertEquals(100, score);
    }

    @Test
    void extremelyHighRttAndLossScoreZeroAndAreUnstable() {
        // Arrange
        double rtt = 1_000_000.0;
        double packetLoss = 1_000.0;

        // Act
        QualityClassifier.QualityAssessment result = classifier.assess(rtt, packetLoss);

        // Assert: o score não fica negativo mesmo muito acima das referências
        assertEquals(0, result.qualityScore());
        assertEquals("UNSTABLE", result.quality().name());
    }

    @Test
    void scoreAtModerateThresholdsIsSeventySix() {
        // rtt 60:  (1 - 60/200) * 0.7 * 100 = 49
        // perda 1: (1 - 1/10)   * 0.3 * 100 = 27
        int score = classifier.score(60.0, 1.0);

        assertEquals(76, score);
    }

    // ---------- Caminho triste: entrada inválida ----------

    @Test
    void negativeRttIsRejected() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> classifier.classify(-1.0, 0.0));

        assertEquals("Prediction metrics must be finite and non-negative", error.getMessage());
    }

    @Test
    void negativePacketLossIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> classifier.classify(30.0, -0.1));
    }

    @Test
    void nanRttIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> classifier.classify(Double.NaN, 0.0));
    }

    @Test
    void infinitePacketLossIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> classifier.classify(30.0, Double.POSITIVE_INFINITY));
    }

    @Test
    void scoreRejectsInvalidInputTheSameWayAsClassify() {
        assertThrows(IllegalArgumentException.class, () -> classifier.score(-5.0, 0.0));
    }
}
