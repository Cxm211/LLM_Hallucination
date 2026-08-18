public void atan2(final double[] y, final int yOffset,
                      final double[] x, final int xOffset,
                      final double[] result, final int resultOffset) {

        // compute r = sqrt(x^2+y^2)
        double[] tmp1 = new double[getSize()];
        multiply(x, xOffset, x, xOffset, tmp1, 0);      // x^2
        double[] tmp2 = new double[getSize()];
        multiply(y, yOffset, y, yOffset, tmp2, 0);      // y^2
        add(tmp1, 0, tmp2, 0, tmp2, 0);                 // x^2 + y^2
        rootN(tmp2, 0, 2, tmp1, 0);                     // r = sqrt(x^2 + y^2)

        if (x[xOffset] >= 0) {

            // compute atan2(y, x) = 2 atan(y / (r + x))
            add(tmp1, 0, x, xOffset, tmp2, 0);          // r + x
            divide(y, yOffset, tmp2, 0, tmp1, 0);       // y /(r + x)
            atan(tmp1, 0, tmp2, 0);                     // atan(y / (r + x))
            for (int i = 0; i < tmp2.length; ++i) {
                result[resultOffset + i] = 2 * tmp2[i]; // 2 * atan(y / (r + x))
            }

        } else {

            // compute atan2(y, x) = +/- pi - 2 atan(y / (r - x))
            subtract(tmp1, 0, x, xOffset, tmp2, 0);     // r - x
            divide(y, yOffset, tmp2, 0, tmp1, 0);       // y /(r - x)
            atan(tmp1, 0, tmp2, 0);                     // atan(y / (r - x))
            // choose sign based on y, not atan value, to handle y = +/-0 correctly
            double s = FastMath.copySign(FastMath.PI, y[yOffset]);
            result[resultOffset] = s - 2 * tmp2[0];
            for (int i = 1; i < tmp2.length; ++i) {
                result[resultOffset + i] = -2 * tmp2[i]; // +/-pi - 2 * atan(y / (r - x))
            }

        }

        // fix value to take special cases (+0/+0, +0/-0, -0/+0, -0/-0, +/-infinity) correctly
        double y0 = y[yOffset];
        double x0 = x[xOffset];
        double v = result[resultOffset];
        if (Double.isNaN(v) || Double.isInfinite(y0) || Double.isInfinite(x0) || (y0 == 0.0 && x0 == 0.0)) {
            result[resultOffset] = FastMath.atan2(y0, x0);
        }

    }