// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
public void testLeftSideSmallF0() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                public double value(double x) {
                    return x - (1.0 + 1e-10);
                }
            };
        final RegulaFalsiSolver solver = new RegulaFalsiSolver(1e-6, 1e-6, AllowedSolution.LEFT_SIDE);
        solver.setFunctionValueAccuracy(1e-6);
        final double root = solver.solve(100, f, 1.0, 5.0);
        Assert.assertEquals(1.0, root, 1e-6);
    }
