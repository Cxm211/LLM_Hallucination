public Complex tanh() {
        if (isNaN) {
            return NaN;
        }
        // Infinite imaginary part yields NaN
        if (Double.isInfinite(imaginary)) {
            return NaN;
        }
        // Infinite real part tends to +/-1
        if (Double.isInfinite(real)) {
            return createComplex(FastMath.copySign(1.0, real), 0.0);
        }
        // Avoid overflow for large real values
        if (FastMath.abs(real) > 20.0) {
            return createComplex(FastMath.copySign(1.0, real), 0.0);
        }

        double real2 = 2.0 * real;
        double imaginary2 = 2.0 * imaginary;
        double d = FastMath.cosh(real2) + FastMath.cos(imaginary2);

        return createComplex(FastMath.sinh(real2) / d,
                             FastMath.sin(imaginary2) / d);
    }