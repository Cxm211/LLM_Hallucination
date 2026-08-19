// ===== FIXED org.apache.commons.math.complex.Complex :: divide(Complex) [lines 251-281] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-46-fixed/src/main/java/org/apache/commons/math/complex/Complex.java =====
    public Complex divide(Complex divisor)
        throws NullArgumentException {
        MathUtils.checkNotNull(divisor);
        if (isNaN || divisor.isNaN) {
            return NaN;
        }

        if (divisor.isZero) {
            // return isZero ? NaN : INF; // See MATH-657
            return NaN;
        }

        if (divisor.isInfinite() && !isInfinite()) {
            return ZERO;
        }

        final double c = divisor.getReal();
        final double d = divisor.getImaginary();

        if (FastMath.abs(c) < FastMath.abs(d)) {
            double q = c / d;
            double denominator = c * q + d;
            return createComplex((real * q + imaginary) / denominator,
                (imaginary * q - real) / denominator);
        } else {
            double q = d / c;
            double denominator = d * q + c;
            return createComplex((imaginary * q + real) / denominator,
                (imaginary - real * q) / denominator);
        }
    }

// ===== FIXED org.apache.commons.math.complex.Complex :: divide(double) [lines 291-304] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-46-fixed/src/main/java/org/apache/commons/math/complex/Complex.java =====
    public Complex divide(double divisor) {
        if (isNaN || Double.isNaN(divisor)) {
            return NaN;
        }
        if (divisor == 0d) {
            // return isZero ? NaN : INF; // See MATH-657
            return NaN;
        }
        if (Double.isInfinite(divisor)) {
            return !isInfinite() ? ZERO : NaN;
        }
        return createComplex(real / divisor,
                             imaginary  / divisor);
    }
