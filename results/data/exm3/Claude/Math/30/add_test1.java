// org/apache/commons/math3/stat/inference/MannWhitneyUTestTest.java
@Test
    public void testIdenticalDataSets() throws Exception {
        double[] d1 = new double[100];
        double[] d2 = new double[100];
        for (int i = 0; i < 100; i++) {
            d1[i] = i;
            d2[i] = i;
        }
        double result = testStatistic.mannWhitneyUTest(d1, d2);
        Assert.assertTrue(result > 0.9);
    }