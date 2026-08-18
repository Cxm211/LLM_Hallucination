// org/apache/commons/math/analysis/solvers/BrentSolverTest.java::testEndpointIsRootMin
public void testEndpointIsRootMin() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 2.0; }
        };
        UnivariateRealSolver solver = new BrentSolver();
        double root = solver.solve(f, 2.0, 4.0, 3.0);
        assertEquals(2.0, root, solver.getAbsoluteAccuracy());
    }