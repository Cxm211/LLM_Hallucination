// org/apache/commons/math/analysis/solvers/BracketingNthOrderBrentSolverTest.java::testIssue716
@Test
    public void testIssue716_leftStart() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 1.0e-22, 5);
        UnivariateFunction sharpTurn = new UnivariateFunction() {
            public double value(double x) {
                return (2 * x + 1) / (1.0e9 * (x + 1));
            }
        };
        double result = solver.solve(100, sharpTurn, -0.999, 30, -0.9, AllowedSolution.LEFT_SIDE);
        Assert.assertEquals(0, sharpTurn.value(result), solver.getFunctionValueAccuracy());
        Assert.assertTrue(sharpTurn.value(result) <= 0);
        Assert.assertEquals(-0.5, result, 1.0e-10);
    }