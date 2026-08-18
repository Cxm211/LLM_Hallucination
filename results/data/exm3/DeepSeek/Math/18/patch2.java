        public boolean isFeasible(final double[] x) {
            if (boundaries == null) {
                return true;
            }
            final double eps = 1e-12;
            for (int i = 0; i < x.length; i++) {
                double diff = boundaries[1][i] - boundaries[0][i];
                if (diff == 0.0) {
                    continue;
                }
                if (x[i] < -eps) {
                    return false;
                }
                if (x[i] > 1.0 + eps) {
                    return false;
                }
            }
            return true;
        }