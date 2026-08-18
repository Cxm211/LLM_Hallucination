        public boolean isFeasible(final double[] x) {
            if (boundaries == null) {
                return true;
            }

            final double eps = 1e-15;
            for (int i = 0; i < x.length; i++) {
                if (x[i] < -eps) {
                    return false;
                }
                if (x[i] > 1.0 + eps) {
                    return false;
                }
            }
            return true;
        }