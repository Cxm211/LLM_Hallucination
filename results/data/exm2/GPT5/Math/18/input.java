        public double[] encode(final double[] x) {
            if (boundaries == null) {
                return x;
            }
            double[] res = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                double diff = boundaries[1][i] - boundaries[0][i];
                res[i] = (x[i] - boundaries[0][i]) / diff;
            }
            return res;
        }

        public double[] decode(final double[] x) {
            if (boundaries == null) {
                return x;
            }
            double[] res = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                double diff = boundaries[1][i] - boundaries[0][i];
                res[i] = diff * x[i] + boundaries[0][i];
            }
            return res;
        }

        public boolean isFeasible(final double[] x) {
            if (boundaries == null) {
                return true;
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

// trigger testcase
@Test
    public void testFitAccuracyDependsOnBoundary() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 11.1;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 1 };
 
        // No bounds.
        PointValuePair result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                                   start);
        final double resNoBound = result.getPoint()[0];

        // Optimum is near the lower bound.
        final double[] lower = { -20 };
        final double[] upper = { 5e16 };
        result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                    start, lower, upper);
        final double resNearLo = result.getPoint()[0];

        // Optimum is near the upper bound.
        lower[0] = -5e16;
        upper[0] = 20;
        result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                    start, lower, upper);
        final double resNearHi = result.getPoint()[0];

        // System.out.println("resNoBound=" + resNoBound +
        //                    " resNearLo=" + resNearLo +
        //                    " resNearHi=" + resNearHi);

        // The two values currently differ by a substantial amount, indicating that
        // the bounds definition can prevent reaching the optimum.
        Assert.assertEquals(resNoBound, resNearLo, 1e-3);
        Assert.assertEquals(resNoBound, resNearHi, 1e-3);
    }
