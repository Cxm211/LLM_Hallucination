// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java::testIssue631LeftSide
public void testIssue631LeftSide() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                public double value(double x) {
                    return Math.exp(x) - Math.pow(Math.PI, 3.0);
                }
            };
        final UnivariateRealSolver solver = new RegulaFalsiSolver(AllowedSolution.LEFT_SIDE);
        final double root = solver.solve(5000, f, 1, 10);
        // Must be on or to the left of the actual root and bracketed
        Assert.assertTrue(root <= 3.4341896575482003);
        Assert.assertTrue(f.value(root) <= 0.0);
    }