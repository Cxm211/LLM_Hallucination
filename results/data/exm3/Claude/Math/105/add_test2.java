// org/apache/commons/math/stat/regression/SimpleRegressionTest.java
public void testSSEWithConstantX() {
    double[] y = { 1.0, 2.0, 3.0 };
    double[] x = { 5.0, 5.0, 5.0 };
    SimpleRegression reg = new SimpleRegression();
    for (int i = 0; i < x.length; i++) {
        reg.addData(x[i], y[i]);
    }
    double sse = reg.getSumSquaredErrors();
    assertTrue(sse >= 0.0);
}