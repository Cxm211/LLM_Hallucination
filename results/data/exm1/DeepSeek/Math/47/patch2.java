public Complex divide(double divisor) {
    if (isNaN || Double.isNaN(divisor)) {
        return NaN;
    }
    if (divisor == 0d) {
        if (real == 0.0 && imaginary == 0.0) {
            return NaN;
        }
        return Complex.INF;
    }
    if (Double.isInfinite(divisor)) {
        return !isInfinite() ? ZERO : NaN;
    }
    return createComplex(real / divisor,
                         imaginary  / divisor);
}