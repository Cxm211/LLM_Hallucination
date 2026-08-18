// org/apache/commons/math/analysis/solvers/BrentSolverTest.java
public void testBugMinInitialBracket() throws Exception {
        UnivariateRealFunction f = new org.apache.commons.math.analysis.polynomials.PolynomialFunction(new double[]{-4, 0, 1});
        UnivariateRealSolver solver = new BrentSolver();
        double result = solver.solve(f, 0, 5, 3);
        assertEquals(2, result, solver.getAbsoluteAccuracy());
    }
