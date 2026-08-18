    public Complex tan() {
        if (isNaN) {
            return NaN;
        }

        double real2 = 2.0 * real;
        double imaginary2 = 2.0 * imaginary;
        double d = FastMath.cos(real2) + FastMath.cosh(imaginary2);

        if (Double.isInfinite(imaginary2)) {
            if (imaginary2 > 0) {
                return createComplex(0.0, 1.0);
            } else {
                return createComplex(0.0, -1.0);
            }
        }
        if (Double.isInfinite(real2) || Double.isInfinite(d)) {
            return NaN;
        }

        return createComplex(FastMath.sin(real2) / d,
                             FastMath.sinh(imaginary2) / d);
    }