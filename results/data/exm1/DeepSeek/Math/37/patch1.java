public Complex tanh() {
    if (isNaN) {
        return NaN;
    }
    if (Double.isInfinite(real)) {
        if (Double.isFinite(imaginary)) {
            return createComplex(Math.signum(real), 0.0);
        } else {
            return NaN;
        }
    }
    if (Double.isInfinite(imaginary)) {
        return NaN;
    }
    double real2 = 2.0 * real;
    double imaginary2 = 2.0 * imaginary;
    double d = FastMath.cosh(real2) + FastMath.cos(imaginary2);
    if (d == 0.0) {
        return createComplex(Double.NaN, Double.POSITIVE_INFINITY);
    }
    return createComplex(FastMath.sinh(real2) / d,
                         FastMath.sin(imaginary2) / d);
}