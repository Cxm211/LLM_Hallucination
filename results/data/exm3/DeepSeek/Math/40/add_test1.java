// org/apache/commons/math/analysis/solvers/BracketingNthOrderBrentSolverTest.java
@Test
    public void testIssue716_BelowAbove() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 1.0e-22, 5);
        UnivariateFunction sharpTurn = new UnivariateFunction() {
            public double value(double x) {
                return (2 * x + 1) / (1.0e9 * (x + 1));
            }
        };
        // BELOW_SIDE: should return point where function <= 0
        double resultBelow = solver.solve(100, sharpTurn, -0.9999999, 30, 15, AllowedSolution.BELOW_SIDE);
        Assert.assertEquals(0, sharpTurn.value(resultBelow), solver.getFunctionValueAccuracy());
        Assert.assertTrue(sharpTurn.value(resultBelow) <= 0);
        Assert.assertEquals(-0.5, resultBelow, 1.0e-10);
        // ABOVE_SIDE: should return point where function >= 0
        double resultAbove = solver.solve(100, sharpTurn, -0.9999999, 30, 15, AllowedSolution.ABOVE_SIDE);
        Assert.assertEquals(0, sharpTurn.value(resultAbove), solver.getFunctionValueAccuracy());
        Assert.assertTrue(sharpTurn.value(resultAbove) >= 0);
        Assert.assertEquals(-0.5, resultAbove, 1.0e-10);
    }
