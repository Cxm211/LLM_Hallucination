public boolean isFeasible(final double[] x) {
            if (boundaries == null) {
                return true;
            }
            for (int i = 0; i < x.length; i++) {
                final double lo = boundaries[0][i];
                final double hi = boundaries[1][i];
                if (x[i] < lo) {
                    return false;
                }
                if (x[i] > hi) {
                    return false;
                }
            }
            return true;
        }