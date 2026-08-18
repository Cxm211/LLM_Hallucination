// org/apache/commons/math3/optimization/direct/CMAESOptimizerTest.java::testBoundaryRangeTooLargeMixedDimensions
public void testBoundaryRangeTooLargeMixedDimensions() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
            public double value(double[] parameters) {
                if (Double.isNaN(parameters[0]) || Double.isNaN(parameters[1])) {
                    throw new MathIllegalStateException();
                }
                final double t0 = 1.0;
                final double t1 = 2.0;
                final double e0 = t0 - parameters[0];
                final double e1 = t1 - parameters[1];
                return e0 * e0 + e1 * e1;
            }
        };

        final double[] start = { 0.0, 0.0 };
        final double max = Double.MAX_VALUE / 2;
        final double tooLarge = FastMath.nextUp(max);
        final double[] lower = { -tooLarge, -1.0 };
        final double[] upper = {  tooLarge,  3.0 };

        optimizer.optimize(10000, fitnessFunction, GoalType.MINIMIZE, start, lower, upper).getPoint();
    }