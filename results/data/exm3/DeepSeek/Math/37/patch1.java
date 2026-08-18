    public Complex tanh() {
        if (isNaN) {
            return NaN;
        }
        double real2 = 2.0 * real;
        double imaginary2 = 2.0 * imaginary;
        double d = FastMath.cosh(real2) + FastMath.cos(imaginary2);

        if (Double.isInfinite(d)) {
            // cosh(real2) is infinite
            return createComplex(FastMath.tanh(real2), 0.0);
        }

        return createComplex(FastMath.sinh(real2) / d,
                             FastMath.sin(imaginary2) / d);
    }