public Complex tanh() {
        if (isNaN) {
            return NaN;
        }
        // Use overflow-avoiding formulation based on sech(2x) and tanh(2x)
        // tanh(x+iy) = [sinh(2x) / (cosh(2x)+cos(2y))] + i [sin(2y) / (cosh(2x)+cos(2y))]
        // Let s = sech(2x) and t = tanh(2x). Then:
        //   real = t / (1 + cos(2y) * s)
        //   imag = (sin(2y) * s) / (1 + cos(2y) * s)
        final double x = real;
        final double y = imaginary;

        final double twoY = 2.0 * y;
        final double cos2y = FastMath.cos(twoY);
        final double sin2y = FastMath.sin(twoY);

        final double ax = FastMath.abs(x);
        final double u = FastMath.exp(-2.0 * ax); // in [0, 1]
        final double u2 = u * u;
        final double sech2x = (2.0 * u) / (1.0 + u2);
        final double tanh2x = (x >= 0.0 ? 1.0 : -1.0) * (1.0 - u2) / (1.0 + u2);

        final double den = 1.0 + cos2y * sech2x;
        return createComplex(tanh2x / den,
                             (sin2y * sech2x) / den);
    }