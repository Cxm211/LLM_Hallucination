// org/apache/commons/math/stat/inference/ChiSquareTestTest.java
public void testChiSquareZeroObservedSum() throws Exception {
        long[] observed = {0, 0, 0};
        double[] expected = {1.0, 2.0, 3.0};
        try {
            testStatistic.chiSquare(expected, observed);
            fail("observed counts sum to zero, IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }
