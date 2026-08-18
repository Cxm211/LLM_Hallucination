// ===== FIXED org.apache.commons.math3.complex.Complex :: reciprocal() [lines 299-321] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-5-fixed/src/main/java/org/apache/commons/math3/complex/Complex.java =====
    public Complex reciprocal() {
        if (isNaN) {
            return NaN;
        }

        if (real == 0.0 && imaginary == 0.0) {
            return INF;
        }

        if (isInfinite) {
            return ZERO;
        }

        if (FastMath.abs(real) < FastMath.abs(imaginary)) {
            double q = real / imaginary;
            double scale = 1. / (real * q + imaginary);
            return createComplex(scale * q, -scale);
        } else {
            double q = imaginary / real;
            double scale = 1. / (imaginary * q + real);
            return createComplex(scale, -scale * q);
        }
    }
