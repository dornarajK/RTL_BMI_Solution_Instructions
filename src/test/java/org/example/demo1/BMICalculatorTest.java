package org.example.demo1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BMICalculatorTest {
    @Test
    void calculatesTypicalAdultBmi() {
        assertEquals(22.86, BMICalculator.calculate(70.0, 175.0), 0.01);
    }

    @Test
    void calculatesAnotherValidBmi() {
        assertEquals(24.22, BMICalculator.calculate(62.0, 160.0), 0.01);
    }

    @Test
    void rejectsZeroWeight() {
        assertThrows(IllegalArgumentException.class, () -> BMICalculator.calculate(0.0, 170.0));
    }

    @Test
    void rejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class, () -> BMICalculator.calculate(-1.0, 170.0));
    }

    @Test
    void rejectsZeroHeight() {
        assertThrows(IllegalArgumentException.class, () -> BMICalculator.calculate(70.0, 0.0));
    }

    @Test
    void rejectsNegativeHeight() {
        assertThrows(IllegalArgumentException.class, () -> BMICalculator.calculate(70.0, -10.0));
    }

    @Test
    void rejectsNanWeight() {
        assertThrows(IllegalArgumentException.class, () -> BMICalculator.calculate(Double.NaN, 170.0));
    }

    @Test
    void rejectsInfiniteHeight() {
        assertThrows(IllegalArgumentException.class, () -> BMICalculator.calculate(70.0, Double.POSITIVE_INFINITY));
    }

    @Test
    void rejectsNonFiniteResult() {
        assertThrows(IllegalArgumentException.class, () -> BMICalculator.calculate(Double.MAX_VALUE, 0.01));
    }

    @Test
    void rejectsNullNumericInputAtUiParsingBoundary() {
        assertThrows(NullPointerException.class, () -> Double.parseDouble(null));
    }
}
