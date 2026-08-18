// org/apache/commons/math/analysis/solvers/BisectionSolverTest.java
public void testMath369_AdditionalCase1() throws Exception {
    UnivariateRealFunction f = new UnivariateRealFunction() {
        public double value(double x) {
            return x * x - 4.0;
        }
    };
    UnivariateRealSolver solver = new BisectionSolver();
    assertEquals(2.0, solver.solve(f, 1.5, 2.5, 2.0), solver.getAbsoluteAccuracy());
}