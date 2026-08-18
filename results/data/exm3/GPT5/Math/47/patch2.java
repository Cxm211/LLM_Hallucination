public Complex divide(double divisor) {
        if (isNaN || Double.isNaN(divisor)) {
            return NaN;
        }
        if (divisor == 0d) {
            // x / 0 -> INF if x != 0; 0 / 0 -> NaN
            if (real == 0.0 && imaginary == 0.0) {
                return NaN;
            }
            return INF;
        }
        if (Double.isInfinite(divisor)) {
            return !isInfinite() ? ZERO : NaN;
        }
        return createComplex(real / divisor,
                             imaginary  / divisor);
    }