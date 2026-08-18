// org/apache/commons/math/stat/regression/SimpleRegressionTest.java
public void testSSEWithSinglePoint() {
    SimpleRegression reg = new SimpleRegression();
    reg.addData(5.0, 10.0);
    double sse = reg.getSumSquaredErrors();
    assertTrue(sse >= 0.0);
    assertEquals(0.0, sse, 1e-10);
}