public Complex tan() {
    if (isNaN) {
        return NaN;
    }

    double real2 = 2.0 * real;
    double imaginary2 = 2.0 * imaginary;
    double d = FastMath.cos(real2) + FastMath.cosh(imaginary2);

    if (FastMath.abs(d) < 1.0e-10) {
        return createComplex(Double.POSITIVE_INFINITY * FastMath.sin(real2), Double.NaN);
    }

    return createComplex(FastMath.sin(real2) / d,
                         FastMath.sinh(imaginary2) / d);
}