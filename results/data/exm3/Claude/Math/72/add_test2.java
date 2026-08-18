// org/apache/commons/math/analysis/solvers/BrentSolverTest.java
public void testRootAtNegativeEndpoint() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BrentSolver();

        // negative endpoint is root
        double result = solver.solve(f, -Math.PI, -2);
        assertEquals(-Math.PI, result, solver.getAbsoluteAccuracy());

        result = solver.solve(f, -4, -Math.PI);
        assertEquals(-Math.PI, result, solver.getAbsoluteAccuracy());
    }