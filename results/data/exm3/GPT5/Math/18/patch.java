public double[] encode(final double[] x) {
            if (boundaries == null) {
                return x;
            }
            double[] res = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                final double lo = boundaries[0][i];
                final double hi = boundaries[1][i];
                final double diff = hi - lo;
                double v;
                if (diff == 0.0) {
                    // Variable is fixed; map to center of [0, 1]
                    v = 0.5;
                } else {
                    v = (x[i] - lo) / diff;
                }
                // Ensure within [0, 1]
                if (v < 0.0) {
                    v = 0.0;
                } else if (v > 1.0) {
                    v = 1.0;
                }
                res[i] = v;
            }
            return res;
        }