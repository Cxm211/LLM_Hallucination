// org/apache/commons/math/stat/descriptive/SummaryStatisticsTest.java
@Test
    public void testNoOverride() throws Exception {
        double[] scores = {5, 7, 9, 11};
        SummaryStatistics stats = new SummaryStatistics();
        for (double i : scores) {
            stats.addValue(i);
        }
        Assert.assertEquals(8.0, stats.getMean(), 1e-14);
        Assert.assertEquals(6.666666666666667, stats.getVariance(), 1e-14);
        Assert.assertEquals(7.976644743104381, stats.getGeometricMean(), 1e-14);
    }
