// org/apache/commons/math/stat/regression/SimpleRegressionTest.java
public void testSSEWithPerfectFit() {
    double[] y = { 2.0, 4.0, 6.0, 8.0 };
    double[] x = { 1.0, 2.0, 3.0, 4.0 };
    SimpleRegression reg = new SimpleRegression();
    for (int i = 0; i < x.length; i++) {
        reg.addData(x[i], y[i]);
    }
    double sse = reg.getSumSquaredErrors();
    assertTrue(sse >= 0.0);
    assertTrue(sse < 1e-10);
}