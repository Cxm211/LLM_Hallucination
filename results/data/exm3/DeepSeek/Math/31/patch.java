    public double evaluate(double x, double epsilon, int maxIterations) {
        final double small = 1e-50;
        double hPrev = getA(0, x);

        // use the value of small as epsilon criteria for zero checks
        if (Precision.equals(hPrev, 0.0, small)) {
            hPrev = small;
        }

        int n = 1;
        double dPrev = 0.0;
        double p0 = 1.0;
        double q1 = 1.0;
        double cPrev = hPrev;
        double hN = hPrev;

        while (n < maxIterations) {
            final double a = getA(n, x);
            final double b = getB(n, x);

            double cN = a * hPrev + b * p0;
            double q2 = a * q1 + b * dPrev;
            if (Double.isInfinite(cN) || Double.isInfinite(q2)) {
                double scaleFactor = 1d;
                final int maxPower = 5;
                final double absA = FastMath.abs(a);
                final double absB = FastMath.abs(b);
                final double scale = FastMath.max(absA, absB);
                if (scale > 0) {
                    for (int i = 0; i < maxPower; i++) {
                        scaleFactor *= scale;
                        cN = (a * hPrev) / scaleFactor + (b * p0) / scaleFactor;
                        q2 = (a * q1) / scaleFactor + (b * dPrev) / scaleFactor;
                        if (!(Double.isInfinite(cN) || Double.isInfinite(q2))) {
                            break;
                        }
                    }
                } else {
                    // scale is zero, meaning a and b are zero, so cN and q2 should be zero, but if infinite, set to zero
                    cN = 0.0;
                    q2 = 0.0;
                }
            }

            final double deltaN = cN / q2 / cPrev;
            hN = cPrev * deltaN;

            if (Double.isInfinite(hN)) {
                throw new ConvergenceException(LocalizedFormats.CONTINUED_FRACTION_INFINITY_DIVERGENCE,
                                               x);
            }
            if (Double.isNaN(hN)) {
                throw new ConvergenceException(LocalizedFormats.CONTINUED_FRACTION_NAN_DIVERGENCE,
                                               x);
            }

            if (FastMath.abs(deltaN - 1.0) < epsilon) {
                break;
            }

            dPrev = q1;
            cPrev = cN / q2;
            p0 = hPrev;
            hPrev = cN;
            q1 = q2;
            n++;
        }

        if (n >= maxIterations) {
            throw new MaxCountExceededException(LocalizedFormats.NON_CONVERGENT_CONTINUED_FRACTION,
                                                maxIterations, x);
        }

        return hN;
    }