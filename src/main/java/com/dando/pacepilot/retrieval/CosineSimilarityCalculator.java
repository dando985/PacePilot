package com.dando.pacepilot.retrieval;

import org.springframework.stereotype.Component;

@Component
public class CosineSimilarityCalculator {

    public double calculate(double[] firstVector, double[] secondVector) {
        validateVectors(firstVector, secondVector);

        double dotProduct = 0.0;
        double firstMagnitudeSquared = 0.0;
        double secondMagnitudeSquared = 0.0;

        for (int index = 0; index < firstVector.length; index++) {

            dotProduct += firstVector[index] * secondVector[index];

            firstMagnitudeSquared += firstVector[index] * firstVector[index];

            secondMagnitudeSquared += secondVector[index] * secondVector[index];
        }

        if (firstMagnitudeSquared == 0.0 || secondMagnitudeSquared == 0.0) {
            throw new IllegalArgumentException("Cosine similarity cannot be calculated for a zero vector.");
        }

        double firstMagnitude = Math.sqrt(firstMagnitudeSquared);

        double secondMagnitude = Math.sqrt(secondMagnitudeSquared);

        double similarity = dotProduct / (firstMagnitude * secondMagnitude);

        return Math.max(-1.0, Math.min(1.0, similarity));
    }

    private void validateVectors(double[] firstVector, double[] secondVector) {
        if (firstVector == null || secondVector == null) {
            throw new IllegalArgumentException("Vectors must not be null.");
        }

        if (firstVector.length == 0 || secondVector.length == 0) {
            throw new IllegalArgumentException("Vectors must not be empty.");
        }

        if (firstVector.length != secondVector.length) {
            throw new IllegalArgumentException("Vectors must have the same number of dimensions.");
        }
    }
}