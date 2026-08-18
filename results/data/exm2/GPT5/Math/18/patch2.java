        public boolean isFeasible(final double[] x) {
            if (boundaries == null) {
                return true;
            }


            for (int i = 0; i < x.length; i++) {
                if (x[i] < boundaries[0][i]) {
                    return false;
                }
                if (x[i] > boundaries[1][i]) {
                    return false;
                }
            }
            return true;
        }