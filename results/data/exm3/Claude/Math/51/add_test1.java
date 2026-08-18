// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
@Test
    public void testIssue631_MultipleIterations() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                @Override
                public double value(double x) {
                    return x * x * x - 8.0;
                }
            };

        final UnivariateRealSolver solver = new RegulaFalsiSolver();
        final double root = solver.solve(3624, f, 0, 5);
        Assert.assertEquals(2.0, root, 1e-15);
    }