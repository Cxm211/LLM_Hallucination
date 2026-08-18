// buggy function
    public void addValue(double value) {
        sumImpl.increment(value);
        sumsqImpl.increment(value);
        minImpl.increment(value);
        maxImpl.increment(value);
        sumLogImpl.increment(value);
        secondMoment.increment(value);
        // If mean, variance or geomean have been overridden,
        // need to increment these
        if (!(meanImpl instanceof Mean)) {
            meanImpl.increment(value);
        }
        if (!(varianceImpl instanceof Variance)) {
            varianceImpl.increment(value);
        }
        if (!(geoMeanImpl instanceof GeometricMean)) {
            geoMeanImpl.increment(value);
        }
        n++;
    }

// trigger testcase
// org/apache/commons/math/stat/descriptive/SummaryStatisticsTest.java::testOverrideGeoMeanWithMathClass
@Test
    public void testOverrideGeoMeanWithMathClass() throws Exception {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setGeoMeanImpl(new GeometricMean()); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new GeometricMean()).evaluate(scores),stats.getGeometricMean(), 0); 
    }

// org/apache/commons/math/stat/descriptive/SummaryStatisticsTest.java::testOverrideMeanWithMathClass
@Test
    public void testOverrideMeanWithMathClass() throws Exception {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setMeanImpl(new Mean()); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new Mean()).evaluate(scores),stats.getMean(), 0); 
    }

// org/apache/commons/math/stat/descriptive/SummaryStatisticsTest.java::testOverrideVarianceWithMathClass
@Test
    public void testOverrideVarianceWithMathClass() throws Exception {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setVarianceImpl(new Variance(false)); //use "population variance"
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new Variance(false)).evaluate(scores),stats.getVariance(), 0); 
    }

// org/apache/commons/math/stat/descriptive/SummaryStatisticsTest.java::testOverrideGeoMeanWithMathClass
@Test
    public void testOverrideGeoMeanWithMathClass() throws Exception {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setGeoMeanImpl(new GeometricMean()); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new GeometricMean()).evaluate(scores),stats.getGeometricMean(), 0); 
    }

// org/apache/commons/math/stat/descriptive/SummaryStatisticsTest.java::testOverrideMeanWithMathClass
@Test
    public void testOverrideMeanWithMathClass() throws Exception {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setMeanImpl(new Mean()); 
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new Mean()).evaluate(scores),stats.getMean(), 0); 
    }

// org/apache/commons/math/stat/descriptive/SummaryStatisticsTest.java::testOverrideVarianceWithMathClass
@Test
    public void testOverrideVarianceWithMathClass() throws Exception {
        double[] scores = {1, 2, 3, 4};
        SummaryStatistics stats = new SummaryStatistics();
        stats.setVarianceImpl(new Variance(false)); //use "population variance"
        for(double i : scores) {
          stats.addValue(i);
        }
        Assert.assertEquals((new Variance(false)).evaluate(scores),stats.getVariance(), 0); 
    }
