// org/apache/commons/math/analysis/solvers/BrentSolverTest.java
public void testRootAtMax() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) { return x - 3.0; }
        };
        UnivariateRealSolver solver = new BrentSolver();
        double result = solver.solve(f, 0.0, 3.0, 2.0);
        assertEquals(3.0, result, 1.0e-6);
    }
