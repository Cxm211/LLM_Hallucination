// org/apache/commons/math/analysis/solvers/BrentSolverTest.java
public void testRootAtMinEndpointWithInitialEqualsMin() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BrentSolver();

        // endpoint is root and initial guess equals the endpoint
        double result = solver.solve(f, Math.PI, 4, Math.PI);
        assertEquals(Math.PI, result, solver.getAbsoluteAccuracy());
    }