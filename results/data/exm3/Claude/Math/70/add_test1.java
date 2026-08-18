// org/apache/commons/math/analysis/solvers/BisectionSolverTest.java
public void testMath369_AdditionalCase2() throws Exception {
    UnivariateRealFunction f = new UnivariateRealFunction() {
        public double value(double x) {
            return Math.exp(x) - 2.0;
        }
    };
    UnivariateRealSolver solver = new BisectionSolver();
    double result = solver.solve(f, 0.0, 1.0, 0.5);
    assertTrue(Math.abs(Math.exp(result) - 2.0) < solver.getAbsoluteAccuracy());
}