// org/apache/commons/math/stat/inference/ChiSquareTestTest.java
public void testChiSquareMinimalLength() throws Exception {
        long[] observed = {5, 10};
        double[] expected = {7.5, 7.5};
        assertEquals("chi-square statistic", 2.0833333333333335, testStatistic.chiSquare(expected, observed), 1E-10);
    }