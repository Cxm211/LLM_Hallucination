public Complex tan() {
        if (isNaN) {
            return NaN;
        }

        // Handle infinities explicitly
        if (Double.isInfinite(imaginary)) {
            if (Double.isFinite(real)) {
                return createComplex(0.0, FastMath.copySign(1.0, imaginary));
            } else {
                return NaN;
            }
        }
        if (Double.isInfinite(real)) {
            return NaN;
        }

        // Large imaginary part: tan(x + i y) -> i * sign(y)
        if (FastMath.abs(imaginary) > 20) {
            return createComplex(0.0, FastMath.copySign(1.0, imaginary));
        }

        // Critical real values: cos(real) == 0 and imaginary == 0 -> infinite real part
        if (imaginary == 0.0 && FastMath.cos(real) == 0.0) {
            double infSign = FastMath.copySign(Double.POSITIVE_INFINITY, FastMath.sin(real));
            return createComplex(infSign, Double.NaN);
        }

        double real2 = 2.0 * real;
        double imaginary2 = 2.0 * imaginary;
        double d = FastMath.cos(real2) + FastMath.cosh(imaginary2);

        return createComplex(FastMath.sin(real2) / d,
                             FastMath.sinh(imaginary2) / d);
    }