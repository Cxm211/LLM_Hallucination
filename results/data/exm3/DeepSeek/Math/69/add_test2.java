// org/apache/commons/math/stat/correlation/PearsonsCorrelationTest.java
public void testPerfectNegativeCorrelation() throws Exception {
    double[][] data = new double[][] {
        {0, 2},
        {1, 1},
        {2, 0}
    };
    PearsonsCorrelation corrInstance = new PearsonsCorrelation(data);
    double pValue = corrInstance.getCorrelationPValues().getEntry(0, 1);
    assertEquals(0.0, pValue, 1e-15);
}
