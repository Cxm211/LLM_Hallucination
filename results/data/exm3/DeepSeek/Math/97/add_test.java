// org/apache/commons/math/analysis/BrentSolverTest.java
public void testRootEndpointsWithinTolerance() throws Exception {
    // Test case where min is within tolerance (positive small value)
    UnivariateRealFunction f1 = new UnivariateRealFunction() {
        public double value(double x) { return 1e-16; }
    };
    UnivariateRealSolver solver1 = new BrentSolver(f1);
    double result = solver1.solve(0, 1);
    assertEquals(0.0, result, solver1.getAbsoluteAccuracy());

    // Test case where min is within tolerance (negative small value)
    UnivariateRealFunction f2 = new UnivariateRealFunction() {
        public double value(double x) { return -1e-16; }
    };
    UnivariateRealSolver solver2 = new BrentSolver(f2);
    result = solver2.solve(0, 1);
    assertEquals(0.0, result, solver2.getAbsoluteAccuracy());

    // Test case where max is within tolerance, min is not
    UnivariateRealFunction f3 = new UnivariateRealFunction() {
        public double value(double x) { return (x == 2.0) ? 1e-16 : 1.0; }
    };
    UnivariateRealSolver solver3 = new BrentSolver(f3);
    result = solver3.solve(1.0, 2.0);
    assertEquals(2.0, result, solver3.getAbsoluteAccuracy());

    // Test case where both endpoints are within tolerance (positive)
    UnivariateRealFunction f4 = new UnivariateRealFunction() {
        public double value(double x) { return 1e-16; }
    };
    UnivariateRealSolver solver4 = new BrentSolver(f4);
    result = solver4.solve(5.0, 10.0);
    assertEquals(5.0, result, solver4.getAbsoluteAccuracy());
}
