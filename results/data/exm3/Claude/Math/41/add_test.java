// org/apache/commons/math/stat/descriptive/UnivariateStatisticAbstractTest.java
@Test
    public void testEvaluateArraySegmentWeightedSingleElement() {
        UnivariateStatistic statistic = getUnivariateStatistic();
        if (!(statistic instanceof WeightedEvaluation)) {
            return;
        }
        final WeightedEvaluation stat = (WeightedEvaluation) getUnivariateStatistic();
        
        // Test single element segment - should return 0.0 variance
        final double[] singleValue = new double[] {testArray[3]};
        final double[] singleWeight = new double[] {testWeightsArray[3]};
        
        double result1 = stat.evaluate(singleValue, singleWeight);
        double result2 = stat.evaluate(testArray, testWeightsArray, 3, 1);
        
        Assert.assertEquals(0.0, result1, 0);
        Assert.assertEquals(result1, result2, 0);
    }