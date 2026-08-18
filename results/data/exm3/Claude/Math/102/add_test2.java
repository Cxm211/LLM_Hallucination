// org/apache/commons/math/stat/inference/TestUtilsTest.java
public void testChiSquareAllEqual() throws Exception {
        long[] observed = {100, 100, 100, 100};
        double[] expected = {100, 100, 100, 100};
        assertEquals("chi-square statistic", 0.0, TestUtils.chiSquare(expected, observed), 1E-12);
    }