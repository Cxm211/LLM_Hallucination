// org/apache/commons/math/optimization/general/MinpackTest.java
public void testMaxIterationsExceeded() {
    LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
    optimizer.setMaxIterations(5);
    optimizer.setCostRelativeTolerance(1.0e-30);
    optimizer.setParRelativeTolerance(1.0e-30);
    optimizer.setOrthoTolerance(1.0e-30);
    
    final double[] startPoint = new double[] { 100.0, -200.0 };
    
    try {
        optimizer.optimize(new MultivariateVectorialFunction() {
            public double[] value(double[] point) {
                double x = point[0];
                double y = point[1];
                return new double[] {
                    x * x + y * y - 1,
                    x * x * x + y * y * y - 1
                };
            }
        }, new double[] { 1.0, 1.0 }, new double[] { 1.0, 1.0 }, startPoint);
        fail("Expected OptimizationException due to max iterations");
    } catch (OptimizationException oe) {
        assertTrue(oe.getMessage().contains("max") || oe.getMessage().contains("Maximal"));
    } catch (Exception e) {
        fail("Unexpected exception type: " + e.getClass().getName());
    }
}