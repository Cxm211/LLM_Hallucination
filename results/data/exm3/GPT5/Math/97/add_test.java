// org/apache/commons/math/analysis/BrentSolverTest.java
public void testBothEndpointsAreRoots() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BrentSolver(f);
        double result = solver.solve(0.0, 2 * Math.PI);
        assertEquals(result, 0.0, solver.getAbsoluteAccuracy());
    }