// org/apache/commons/math/analysis/solvers/BisectionSolverTest.java::testInitialValueDelegation
public void testInitialValueDelegation() throws Exception {
        UnivariateRealFunction f = new CosFunction();
        UnivariateRealSolver solver = new BisectionSolver();
        assertEquals(Math.PI / 2, solver.solve(f, 1.0, 2.0, 1.6), solver.getAbsoluteAccuracy());
    }