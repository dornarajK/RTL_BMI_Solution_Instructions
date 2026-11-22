package org.example.demo1;


public final class BMICalculator {
    private BMICalculator() {
    }

    public static double calculate(double weightKg, double heightCm) {
        if (!Double.isFinite(weightKg) || weightKg <= 0) {
            throw new IllegalArgumentException("Weight must be a finite value greater than zero.");
        }
        if (!Double.isFinite(heightCm) || heightCm <= 0) {
            throw new IllegalArgumentException("Height must be a finite value greater than zero.");
        }
        double heightMeters = heightCm / 100.0;
        double bmi = weightKg / (heightMeters * heightMeters);
        if (!Double.isFinite(bmi)) {
            throw new IllegalArgumentException("BMI result is outside the supported range.");
        }
        return bmi;
    }
}
