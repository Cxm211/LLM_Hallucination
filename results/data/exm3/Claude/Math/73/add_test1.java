// org/apache/commons/math/analysis/solvers/BrentSolverTest.java
public void testRootAtMaxEndpoint() throws Exception {
    UnivariateRealFunction f = new UnivariateRealFunction() {
        public double value(double x) {
            return x - 2.0;
        }
    };
    UnivariateRealSolver solver = new BrentSolver();
    solver.setAbsoluteAccuracy(1e-6);
    double root = solver.solve(f, 1.0, 2.0, 1.5);
    assertEquals(2.0, root, 1e-6);
}