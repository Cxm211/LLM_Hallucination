// org/apache/commons/math/stat/inference/ChiSquareTestTest.java
public void testChiSquareScalingLength2() throws Exception {
        long[] observed = {1, 2};
        double[] expected = {2.0, 2.0};
        double expectedStatistic = 1.0 / 3.0;
        assertEquals("chi-square statistic with scaling", expectedStatistic, testStatistic.chiSquare(expected, observed), 1E-12);
    }
