// org/apache/commons/math/analysis/solvers/BrentSolverTest.java
public void testRootAtMin() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 1.0; }
        };
        UnivariateRealSolver solver = new BrentSolver();
        double result = solver.solve(f, 1.0, 2.0, 1.5);
        assertEquals(1.0, result, 1.0e-6);
    }
