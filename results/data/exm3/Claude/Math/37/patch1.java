public Complex tanh() {
    if (isNaN) {
        return NaN;
    }
    double real2 = 2.0 * real;
    double imaginary2 = 2.0 * imaginary;
    double d = FastMath.cosh(real2) + FastMath.cos(imaginary2);

    // Handle infinity cases
    if (Double.isInfinite(real2)) {
        // When real part is very large, tanh(real) approaches ±1
        return createComplex(real > 0 ? 1.0 : -1.0, 0.0);
    }
    if (Double.isInfinite(imaginary2) || Double.isInfinite(d)) {
        return NaN;
    }

    return createComplex(FastMath.sinh(real2) / d,
                         FastMath.sin(imaginary2) / d);
}