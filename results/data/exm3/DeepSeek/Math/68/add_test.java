// org/apache/commons/math/optimization/general/MinpackTest.java
public void testSimpleLinear() {
    // Create a linear function: y = 2*x + 3
    MultivariateVectorialFunction f = new MultivariateVectorialFunction() {
        public double[] value(double[] point) {
            double a = point[0];
            double b = point[1];
            double[] values = new double[5];
            for (int i = 0; i < 5; i++) {
                values[i] = a * i + b;
            }
            return values;
        }
    };
    // target values
    double[] target = new double[] { 3, 5, 7, 9, 11 };
    // initial guess
    double[] start = new double[] { 100.0, 100.0 };
    // optimizer
    LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
    VectorialPointValuePair result = optimizer.optimize(f, target, start);
    // Check that parameters are close to (2,3)
    assertEquals(2.0, result.getPoint()[0], 1e-6);
    assertEquals(3.0, result.getPoint()[1], 1e-6);
    // Check cost is small
    assertTrue(result.getValue() < 1e-6);
}
