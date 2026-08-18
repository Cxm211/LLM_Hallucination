public Complex tanh() {
    if (isNaN) {
        return NaN;
    }
    if (Double.isInfinite(real) && !Double.isInfinite(imaginary)) {
        return createComplex(Math.copySign(1.0, real), 0.0);
    }
    if (Double.isInfinite(imaginary)) {
        return NaN;
    }
    double real2 = 2.0 * real;
    double imaginary2 = 2.0 * imaginary;
    double d = FastMath.cosh(real2) + FastMath.cos(imaginary2);
    return createComplex(FastMath.sinh(real2) / d,
                         FastMath.sin(imaginary2) / d);
}