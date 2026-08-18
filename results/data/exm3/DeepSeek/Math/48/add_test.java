// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
public void testIssue631LeftSide() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                public double value(double x) {
                    return Math.exp(x) - Math.pow(Math.PI, 3.0);
                }
            };
        final RegulaFalsiSolver solver = new RegulaFalsiSolver();
        solver.setAllowedSolution(AllowedSolution.LEFT_SIDE);
        final double root = solver.solve(100, f, 1, 10);
        double trueRoot = 3.4341896575482003;
        Assert.assertTrue(root <= trueRoot);
        Assert.assertEquals(0.0, f.value(root), 1e-15);
    }
