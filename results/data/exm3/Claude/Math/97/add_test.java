// org/apache/commons/math/analysis/BrentSolverTest.java
public void testRootEndpointsMinIsRoot() throws Exception {
    UnivariateRealFunction f = new SinFunction();
    UnivariateRealSolver solver = new BrentSolver(f);
    
    // min endpoint is root
    double result = solver.solve(0, 1.5);
    assertEquals(result, 0, solver.getAbsoluteAccuracy());
}