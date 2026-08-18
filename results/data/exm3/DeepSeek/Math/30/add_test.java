// org/apache/commons/math3/stat/inference/MannWhitneyUTestTest.java
@Test
    public void testOverflowInProduct() throws Exception {
        int n1 = 50000;
        int n2 = 50000;
        double[] d1 = new double[n1];
        double[] d2 = new double[n2];
        for (int i = 0; i < n1; i++) {
            d1[i] = 2 * i;
        }
        for (int i = 0; i < n2; i++) {
            d2[i] = 2 * i + 1;
        }
        double result = testStatistic.mannWhitneyUTest(d1, d2);
        Assert.assertFalse(Double.isNaN(result));
        Assert.assertTrue(result >= 0 && result <= 1);
    }
