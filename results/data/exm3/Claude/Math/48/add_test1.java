// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
public void testConvergenceOnRightSide() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                public double value(double x) {
                    return x * x - 2.0;
                }
            };

        final RegulaFalsiSolver solver = new RegulaFalsiSolver();
        final double root = solver.solve(1000, f, 0, 2, AllowedSolution.RIGHT_SIDE);
        Assert.assertTrue(root >= Math.sqrt(2.0));
        Assert.assertTrue(f.value(root) >= 0);
        Assert.assertEquals(Math.sqrt(2.0), root, 1e-6);
    }