public Complex tan() {
    if (isNaN) {
        return NaN;
    }
    if (Double.isInfinite(imaginary)) {
        if (Double.isFinite(real)) {
            return createComplex(0.0, Math.signum(imaginary));
        } else {
            return NaN;
        }
    }
    if (Double.isInfinite(real)) {
        return NaN;
    }
    double real2 = 2.0 * real;
    double imaginary2 = 2.0 * imaginary;
    double d = FastMath.cos(real2) + FastMath.cosh(imaginary2);
    if (d == 0.0) {
        double realPart = Double.POSITIVE_INFINITY;
        if (real < 0) {
            realPart = Double.NEGATIVE_INFINITY;
        }
        return createComplex(realPart, Double.NaN);
    }
    return createComplex(FastMath.sin(real2) / d,
                         FastMath.sinh(imaginary2) / d);
}