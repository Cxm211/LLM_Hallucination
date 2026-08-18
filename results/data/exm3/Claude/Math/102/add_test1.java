// org/apache/commons/math/stat/inference/ChiSquareTestTest.java
public void testChiSquareWithZeroObserved() throws Exception {
        long[] observed = {0, 10, 10};
        double[] expected = {5, 10, 5};
        assertEquals("chi-square statistic", 10.0, testStatistic.chiSquare(expected, observed), 1E-10);
    }