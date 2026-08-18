// org/apache/commons/math3/stat/inference/MannWhitneyUTestTest.java
@Test
    public void testSmallDataSetWithContinuityCorrection() throws Exception {
        double[] d1 = new double[10];
        double[] d2 = new double[10];
        for (int i = 0; i < 10; i++) {
            d1[i] = 2 * i;
            d2[i] = 2 * i + 1;
        }
        double result = testStatistic.mannWhitneyUTest(d1, d2);
        Assert.assertTrue(result > 0.5);
    }