    public Complex tan() {
        if (isNaN) {
            return NaN;
        }

        double real2 = 2.0 * real;
        double imaginary2 = 2.0 * imaginary;
        double d = FastMath.cos(real2) + FastMath.cosh(imaginary2);

        if (Double.isInfinite(d)) {
            // cosh(imaginary2) is infinite
            return createComplex(0.0, FastMath.tanh(imaginary2));
        }

        return createComplex(FastMath.sin(real2) / d,
                             FastMath.sinh(imaginary2) / d);
    }