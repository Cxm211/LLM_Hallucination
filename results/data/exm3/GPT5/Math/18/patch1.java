public double[] decode(final double[] x) {
            if (boundaries == null) {
                return x;
            }
            double[] res = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                final double lo = boundaries[0][i];
                final double hi = boundaries[1][i];
                final double diff = hi - lo;
                double xi = x[i];
                // Ensure within [0, 1]
                if (xi < 0.0) {
                    xi = 0.0;
                } else if (xi > 1.0) {
                    xi = 1.0;
                }
                res[i] = lo + diff * xi;
            }
            return res;
        }