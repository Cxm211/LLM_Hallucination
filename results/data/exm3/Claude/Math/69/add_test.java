// org/apache/commons/math/stat/correlation/PearsonsCorrelationTest.java
public void testPValueWithPerfectCorrelation() throws Exception {
        double[][] data = new double[10][2];
        for (int i = 0; i < 10; i++) {
            data[i][0] = i;
            data[i][1] = i;
        }
        PearsonsCorrelation corrInstance = new PearsonsCorrelation(data);
        RealMatrix pValues = corrInstance.getCorrelationPValues();
        assertTrue(pValues.getEntry(0, 1) >= 0);
        assertTrue(pValues.getEntry(0, 0) == 0);
    }