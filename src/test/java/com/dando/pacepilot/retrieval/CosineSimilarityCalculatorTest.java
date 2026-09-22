package com.dando.pacepilot.retrieval;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CosineSimilarityCalculatorTest {

    private final CosineSimilarityCalculator calculator =
            new CosineSimilarityCalculator();

    @Test
    void returnsOneForIdenticalVectors() {
        double[] firstVector = {1.0, 2.0, 3.0};
        double[] secondVector = {1.0, 2.0, 3.0};
        double result = calculator.calculate(firstVector, secondVector);

        assertEquals(1.0, result, 0.000001);
    }

    @Test
    void calculatesSimilarityBetweenTwoVectors() {
        double[] firstVector = {1.0, 0.0};
        double[] secondVector = {1.0, 1.0};
        double result = calculator.calculate(firstVector, secondVector);

        assertEquals(1.0 / Math.sqrt(2.0), result, 0.000001);
    }

    @Test
    void returnsZeroForPerpendicularVectors() {
        double[] firstVector = {1.0, 0.0};
        double[] secondVector = {0.0, 1.0};
        double result = calculator.calculate(firstVector, secondVector);

        assertEquals(0.0, result, 0.000001);
    }

    @Test
    void returnsNegativeOneForOppositeVectors() {
        double[] firstVector = {1.0, 0.0};
        double[] secondVector = {-1.0, 0.0};
        double result = calculator.calculate(firstVector, secondVector);

        assertEquals(-1.0, result, 0.000001);
    }

    @Test
    void rejectsVectorsWithDifferentDimensions() {
        double[] firstVector = {1.0, 2.0};
        double[] secondVector = {1.0, 2.0, 3.0};

        assertThatIllegalArgumentException()
                .isThrownBy(() ->
                        calculator.calculate(
                                firstVector,
                                secondVector
                        )
                )
                .withMessage(
                        "Vectors must have the same number "
                                + "of dimensions."
                );
    }

    @Test
    void rejectsZeroVector() {
        double[] firstVector = {0.0, 0.0};

        double[] secondVector = {1.0, 2.0};

        assertThatIllegalArgumentException()
                .isThrownBy(() ->
                        calculator.calculate(
                                firstVector,
                                secondVector
                        )
                )
                .withMessage(
                        "Cosine similarity cannot be calculated for a zero vector."
                );
    }
}