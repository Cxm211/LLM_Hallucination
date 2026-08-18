// ===== FIXED org.apache.commons.math.fraction.BigFraction :: doubleValue() [lines 684-695] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-36-fixed/src/main/java/org/apache/commons/math/fraction/BigFraction.java =====
    public double doubleValue() {
        double result = numerator.doubleValue() / denominator.doubleValue();
        if (Double.isNaN(result)) {
            // Numerator and/or denominator must be out of range:
            // Calculate how far to shift them to put them in range.
            int shift = Math.max(numerator.bitLength(),
                                 denominator.bitLength()) - Double.MAX_EXPONENT;
            result = numerator.shiftRight(shift).doubleValue() /
                denominator.shiftRight(shift).doubleValue();
        }
        return result;
    }

// ===== FIXED org.apache.commons.math.fraction.BigFraction :: floatValue() [lines 737-748] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-36-fixed/src/main/java/org/apache/commons/math/fraction/BigFraction.java =====
    public float floatValue() {
        float result = numerator.floatValue() / denominator.floatValue();
        if (Double.isNaN(result)) {
            // Numerator and/or denominator must be out of range:
            // Calculate how far to shift them to put them in range.
            int shift = Math.max(numerator.bitLength(),
                                 denominator.bitLength()) - Float.MAX_EXPONENT;
            result = numerator.shiftRight(shift).floatValue() /
                denominator.shiftRight(shift).floatValue();
        }
        return result;
    }
