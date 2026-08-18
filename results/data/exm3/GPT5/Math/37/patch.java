public Complex tan() {
        if (isNaN) {
            return NaN;
        }

        // Use overflow-avoiding formulation based on sech(2y) and tanh(2y)
        // tan(x+iy) = [sin(2x) / (cos(2x)+cosh(2y))] + i [sinh(2y) / (cos(2x)+cosh(2y))]
        // Let s = sech(2y) and t = tanh(2y). Then:
        //   real = (sin(2x) * s) / (1 + cos(2x) * s)
        //   imag = t / (1 + cos(2x) * s)
        // where s = 2u/(1+u^2), t = sign(y) * (1 - u^2)/(1 + u^2), u = exp(-2|y|)
        final double x = real;
        final double y = imaginary;

        final double twoX = 2.0 * x;
        final double cos2x = FastMath.cos(twoX);
        final double sin2x = FastMath.sin(twoX);

        final double ay = FastMath.abs(y);
        final double u = FastMath.exp(-2.0 * ay); // in [0, 1], underflows to 0 for large |y|
        final double u2 = u * u;
        final double sech2y = (2.0 * u) / (1.0 + u2);
        final double tanh2y = (y >= 0.0 ? 1.0 : -1.0) * (1.0 - u2) / (1.0 + u2);

        final double den = 1.0 + cos2x * sech2y;
        return createComplex((sin2x * sech2y) / den,
                             tanh2y / den);
    }