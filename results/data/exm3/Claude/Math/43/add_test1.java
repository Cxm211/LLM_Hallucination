// org/apache/commons/math/stat/descriptive/SummaryStatisticsTest.java
@Test
public void testMultipleOverrides() throws Exception {
    double[] scores = {1, 3, 5, 7, 9};
    SummaryStatistics stats = new SummaryStatistics();
    stats.setMeanImpl(new Mean());
    stats.setVarianceImpl(new Variance(true));
    stats.setGeoMeanImpl(new GeometricMean());
    for(double i : scores) {
        stats.addValue(i);
    }
    Assert.assertEquals((new Mean()).evaluate(scores), stats.getMean(), 0);
    Assert.assertEquals((new Variance(true)).evaluate(scores), stats.getVariance(), 0.0001);
    Assert.assertEquals((new GeometricMean()).evaluate(scores), stats.getGeometricMean(), 0.0001);
}