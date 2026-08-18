// org/apache/commons/math/analysis/solvers/BracketingNthOrderBrentSolverTest.java
@Test
    public void testAboveSideWithPositiveRoot() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 1.0e-22, 5);
        UnivariateFunction func = new UnivariateFunction() {
            public double value(double x) {
                return x - 0.5;
            }
        };
        double result = solver.solve(100, func, 0.0, 1.0, 0.7, AllowedSolution.ABOVE_SIDE);
        Assert.assertTrue(func.value(result) >= 0);
        Assert.assertEquals(0.5, result, 1.0e-10);
    }