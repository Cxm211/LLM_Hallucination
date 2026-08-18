// org/apache/commons/math/analysis/solvers/RegulaFalsiSolverTest.java
public void testRegulaFalsiNearRoot() {
        final UnivariateRealFunction f = new UnivariateRealFunction() {
                public double value(double x) {
                    return Math.sin(x);
                }
            };

        final UnivariateRealSolver solver = new RegulaFalsiSolver();
        final double root = solver.solve(5000, f, 3.0, 3.2);
        Assert.assertEquals(Math.PI, root, 1e-12);
    }