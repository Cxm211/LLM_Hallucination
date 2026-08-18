// org/apache/commons/math/stat/correlation/PearsonsCorrelationTest.java
public void testHighCorrelationSmallSample() throws Exception {
    int dimension = 15; 
    double[][] data = new double[dimension][2];
    for (int i = 0; i < dimension; i++) {
        data[i][0] = i;
        data[i][1] = i + 0.001 * i;
    }
    PearsonsCorrelation corrInstance = new PearsonsCorrelation(data);
    double pValue = corrInstance.getCorrelationPValues().getEntry(0, 1);
    assertTrue("P-value should be positive for high correlation with n=15", pValue > 0);
}
