// org/apache/commons/math/analysis/solvers/BisectionSolverTest.java
public void testSolveLinear() throws Exception {
        UnivariateRealFunction f = new UnivariateRealFunction() {
            public double value(double x) {
                return x;
            }
        };
        UnivariateRealSolver solver = new BisectionSolver();
        assertEquals(0.0, solver.solve(f, -2.0, 2.0, 0.0), solver.getAbsoluteAccuracy());
    }
