public Complex tanh() {
        if (isNaN) {
            return NaN;
        }

        // Handle infinities explicitly
        if (Double.isInfinite(real)) {
            if (Double.isFinite(imaginary)) {
                return createComplex(FastMath.copySign(1.0, real), 0.0);
            } else {
                return NaN;
            }
        }
        if (Double.isInfinite(imaginary)) {
            return NaN;
        }

        // Large real part: tanh(x + i y) -> sign(x)
        if (FastMath.abs(real) > 20) {
            return createComplex(FastMath.copySign(1.0, real), 0.0);
        }

        // Critical imaginary values: denominator zero when real == 0 and cos(imaginary) == -1
        if (real == 0.0 && FastMath.cos(imaginary) == -1.0) {
            double infSign = FastMath.copySign(Double.POSITIVE_INFINITY, FastMath.sin(imaginary));
            return createComplex(Double.NaN, infSign);
        }

        double real2 = 2.0 * real;
        double imaginary2 = 2.0 * imaginary;
        double d = FastMath.cosh(real2) + FastMath.cos(imaginary2);

        return createComplex(FastMath.sinh(real2) / d,
                             FastMath.sin(imaginary2) / d);
    }