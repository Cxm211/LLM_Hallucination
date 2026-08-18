// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
@Test
    public void testIssue631_NegativeRoot() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                @Override
                public double value(double x) {
                    return Math.exp(x) - 1.5;
                }
            };

        final UnivariateRealSolver solver = new RegulaFalsiSolver();
        final double root = solver.solve(3624, f, -1, 1);
        Assert.assertEquals(Math.log(1.5), root, 1e-15);
    }