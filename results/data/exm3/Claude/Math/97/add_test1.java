// org/apache/commons/math/analysis/BrentSolverTest.java
public void testRootEndpointsMaxIsRoot() throws Exception {
    UnivariateRealFunction f = new SinFunction();
    UnivariateRealSolver solver = new BrentSolver(f);
    
    // max endpoint is root
    double result = solver.solve(-1.5, 0);
    assertEquals(result, 0, solver.getAbsoluteAccuracy());
}