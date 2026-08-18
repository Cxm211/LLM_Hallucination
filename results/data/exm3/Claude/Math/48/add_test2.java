// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
public void testSlowConvergenceCubic() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                public double value(double x) {
                    return x * x * x - 8.0;
                }
            };

        final RegulaFalsiSolver solver = new RegulaFalsiSolver();
        final double root = solver.solve(5000, f, 1, 3);
        Assert.assertEquals(2.0, root, 1e-10);
    }