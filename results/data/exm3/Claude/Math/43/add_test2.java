// org/apache/commons/math/stat/descriptive/SummaryStatisticsTest.java
@Test
public void testNoOverride() throws Exception {
    double[] scores = {10, 20, 30};
    SummaryStatistics stats = new SummaryStatistics();
    for(double i : scores) {
        stats.addValue(i);
    }
    Assert.assertEquals(20.0, stats.getMean(), 0.0001);
    Assert.assertEquals(66.66666666666667, stats.getVariance(), 0.0001);
}