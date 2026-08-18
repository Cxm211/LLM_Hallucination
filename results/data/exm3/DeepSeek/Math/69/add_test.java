// org/apache/commons/math/stat/correlation/PearsonsCorrelationTest.java
public void testPerfectPositiveCorrelation() throws Exception {
    double[][] data = new double[][] {
        {0, 0},
        {1, 1},
        {2, 2}
    };
    PearsonsCorrelation corrInstance = new PearsonsCorrelation(data);
    double pValue = corrInstance.getCorrelationPValues().getEntry(0, 1);
    assertEquals(0.0, pValue, 1e-15);
}
