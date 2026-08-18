// org/apache/commons/math/stat/descriptive/SummaryStatisticsTest.java
@Test
public void testOverrideMeanWithCustomClass() throws Exception {
    double[] scores = {2, 4, 6, 8};
    SummaryStatistics stats = new SummaryStatistics();
    // Set a custom implementation that is not Mean class
    stats.setMeanImpl(new org.apache.commons.math.stat.descriptive.moment.Mean());
    for(double i : scores) {
        stats.addValue(i);
    }
    Assert.assertEquals(5.0, stats.getMean(), 0.0001);
}