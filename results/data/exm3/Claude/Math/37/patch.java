public Complex tan() {
    if (isNaN) {
        return NaN;
    }

    double real2 = 2.0 * real;
    double imaginary2 = 2.0 * imaginary;
    double d = FastMath.cos(real2) + FastMath.cosh(imaginary2);

    // Handle infinity cases
    if (Double.isInfinite(imaginary2)) {
        // When imaginary part is very large, tanh(imaginary) approaches ±1
        return createComplex(0.0, imaginary > 0 ? 1.0 : -1.0);
    }
    if (Double.isInfinite(real2) || Double.isInfinite(d)) {
        return NaN;
    }

    return createComplex(FastMath.sin(real2) / d,
                         FastMath.sinh(imaginary2) / d);
}