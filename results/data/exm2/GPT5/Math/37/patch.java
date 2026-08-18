public Complex tan() {
        if (isNaN) {
            return NaN;
        }
        // Both parts infinite -> NaN
        if (Double.isInfinite(real) && Double.isInfinite(imaginary)) {
            return NaN;
        }
        // Infinite imaginary part
        if (Double.isInfinite(imaginary)) {
            return createComplex(0.0, FastMath.copySign(1.0, imaginary));
        }
        // Infinite real part leads to NaN
        if (Double.isInfinite(real)) {
            return NaN;
        }
        // Avoid overflow for large imaginary values
        if (FastMath.abs(imaginary) > 20.0) {
            return createComplex(0.0, FastMath.copySign(1.0, imaginary));
        }

        double real2 = 2.0 * real;
        double imaginary2 = 2.0 * imaginary;
        double d = FastMath.cos(real2) + FastMath.cosh(imaginary2);

        return createComplex(FastMath.sin(real2) / d,
                             FastMath.sinh(imaginary2) / d);
    }