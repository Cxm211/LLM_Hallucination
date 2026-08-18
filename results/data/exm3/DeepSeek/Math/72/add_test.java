// org/apache/commons/math/analysis/solvers/BrentSolverTest.java
public void testBugLeftEndpointPolynomial() throws Exception {
        UnivariateRealFunction f = new org.apache.commons.math.analysis.polynomials.PolynomialFunction(new double[]{-5, 1});
        UnivariateRealSolver solver = new BrentSolver();
        double result = solver.solve(f, 5, 6, 5.5);
        assertEquals(5, result, solver.getAbsoluteAccuracy());
    }
