// org/apache/commons/math/stat/correlation/PearsonsCorrelationTest.java
public void testPValueWithNegativeHighCorrelation() throws Exception {
        int dimension = 100;
        double[][] data = new double[dimension][2];
        for (int i = 0; i < dimension; i++) {
            data[i][0] = i;
            data[i][1] = -i - 0.5/((double)i + 1);
        }
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(data);
        assertTrue(corrInstance.getCorrelationPValues().getEntry(0, 1) >= 0);
    }