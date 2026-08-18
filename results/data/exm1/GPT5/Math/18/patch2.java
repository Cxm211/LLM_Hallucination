public boolean isFeasible(final double[] x) {
            if (boundaries == null) {
                return true;
            }
            // If boundaries are unsupported (non-finite, zero/negative/overflowing range, or dim mismatch),
            // consider all points feasible (no boundary handling).
            if (boundaries.length < 2 || boundaries[0] == null || boundaries[1] == null ||
                boundaries[0].length != x.length || boundaries[1].length != x.length) {
                return true;
            }
            for (int i = 0; i < x.length; i++) {
                final double lo = boundaries[0][i];
                final double hi = boundaries[1][i];
                final double diff = hi - lo;
                if (!Double.isFinite(lo) || !Double.isFinite(hi) || !Double.isFinite(diff) || diff <= 0.0) {
                    return true;
                }
            }

            for (int i = 0; i < x.length; i++) {
                if (x[i] < 0) {
                    return false;
                }
                if (x[i] > 1.0) {
                    return false;
                }
            }
            return true;
        }