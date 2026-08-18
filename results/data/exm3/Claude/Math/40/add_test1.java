// org/apache/commons/math/analysis/solvers/BracketingNthOrderBrentSolverTest.java
@Test
    public void testBelowSideWithNegativeRoot() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 1.0e-22, 5);
        UnivariateFunction func = new UnivariateFunction() {
            public double value(double x) {
                return x + 0.5;
            }
        };
        double result = solver.solve(100, func, -1.0, 0.0, -0.7, AllowedSolution.BELOW_SIDE);
        Assert.assertTrue(func.value(result) <= 0);
        Assert.assertEquals(-0.5, result, 1.0e-10);
    }