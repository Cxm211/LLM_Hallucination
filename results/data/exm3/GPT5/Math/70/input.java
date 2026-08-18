// buggy function
    public double solve(final UnivariateRealFunction f, double min, double max, double initial)
        throws MaxIterationsExceededException, FunctionEvaluationException {
        return solve(min, max);
    }

// trigger testcase
// org/apache/commons/math/analysis/solvers/BisectionSolverTest.java::testMath369
public void testMath369() throws Exception {
        UnivariateRealFunction f = new SinFunction();
        UnivariateRealSolver solver = new BisectionSolver();
        assertEquals(Math.PI, solver.solve(f, 3.0, 3.2, 3.1), solver.getAbsoluteAccuracy());
    }
