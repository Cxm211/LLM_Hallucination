// org/apache/commons/math/stat/descriptive/UnivariateStatisticAbstractTest.java
@Test
    public void testEvaluateArraySegmentWeightedMiddleSegment() {
        UnivariateStatistic statistic = getUnivariateStatistic();
        if (!(statistic instanceof WeightedEvaluation)) {
            return;
        }
        final WeightedEvaluation stat = (WeightedEvaluation) getUnivariateStatistic();
        
        // Test a middle segment of length 3
        if (testArray.length >= 6) {
            final double[] middleArray = new double[3];
            final double[] middleWeights = new double[3];
            System.arraycopy(testArray, 2, middleArray, 0, 3);
            System.arraycopy(testWeightsArray, 2, middleWeights, 0, 3);
            
            double result1 = stat.evaluate(middleArray, middleWeights);
            double result2 = stat.evaluate(testArray, testWeightsArray, 2, 3);
            
            Assert.assertEquals(result1, result2, 1e-10);
        }
    }