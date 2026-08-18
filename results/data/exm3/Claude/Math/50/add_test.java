// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
public void testRegulaFalsiStagnation() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                public double value(double x) {
                    return x * x * x - 8.0;
                }
            };

        final UnivariateRealSolver solver = new RegulaFalsiSolver();
        final double root = solver.solve(1000, f, 1.5, 2.5);
        Assert.assertEquals(2.0, root, 1e-10);
    }