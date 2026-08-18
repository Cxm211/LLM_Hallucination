public Complex tanh() {
    if (isNaN) {
        return NaN;
    }
    double real2 = 2.0 * real;
    double imaginary2 = 2.0 * imaginary;
    double d = FastMath.cosh(real2) + FastMath.cos(imaginary2);

    if (FastMath.abs(d) < 1.0e-10) {
        return createComplex(Double.NaN, Double.POSITIVE_INFINITY * FastMath.sin(imaginary2));
    }

    return createComplex(FastMath.sinh(real2) / d,
                         FastMath.sin(imaginary2) / d);
}