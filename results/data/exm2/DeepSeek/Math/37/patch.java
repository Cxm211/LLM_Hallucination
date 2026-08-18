public Complex tan() {
    if (isNaN) {
        return NaN;
    }
    if (Double.isInfinite(imaginary) && !Double.isInfinite(real)) {
        return createComplex(0.0, Math.copySign(1.0, imaginary));
    }
    if (Double.isInfinite(real)) {
        return NaN;
    }
    double real2 = 2.0 * real;
    double imaginary2 = 2.0 * imaginary;
    double d = FastMath.cos(real2) + FastMath.cosh(imaginary2);
    return createComplex(FastMath.sin(real2) / d,
                         FastMath.sinh(imaginary2) / d);
}