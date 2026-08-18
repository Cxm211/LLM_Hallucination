// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
@Test
    public void testLeftSide() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                @Override
                public double value(double x) {
                    return x - 2.0;
                }
            };
        final RegulaFalsiSolver solver = new RegulaFalsiSolver();
        solver.setAllowedSolution(AllowedSolution.LEFT_SIDE);
        final double root = solver.solve(100, f, 1.0, 3.0);
        Assert.assertEquals(2.0, root, solver.getAbsoluteAccuracy());
        Assert.assertTrue(root <= 2.0);
    }
