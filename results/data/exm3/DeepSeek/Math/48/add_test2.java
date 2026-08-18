// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
public void testIssue631BelowSide() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                public double value(double x) {
                    return Math.exp(x) - Math.pow(Math.PI, 3.0);
                }
            };
        final RegulaFalsiSolver solver = new RegulaFalsiSolver();
        solver.setAllowedSolution(AllowedSolution.BELOW_SIDE);
        final double root = solver.solve(100, f, 1, 10);
        Assert.assertTrue(f.value(root) <= 0);
        Assert.assertEquals(0.0, f.value(root), 1e-15);
    }
