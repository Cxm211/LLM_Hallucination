// org/apache/commons/math/stat/descriptive/UnivariateStatisticAbstractTest.java::testEvaluateArraySegmentWeightedAdditional
@Test
public void testEvaluateArraySegmentWeightedAdditional() {
    UnivariateStatistic statistic = getUnivariateStatistic();
    if (!(statistic instanceof WeightedEvaluation)) {
        return;
    }
    final WeightedEvaluation stat = (WeightedEvaluation) getUnivariateStatistic();

    // Full arrays with a large weight outside the evaluated segment
    final double[] fullValues = new double[] {1.0, 2.0, 100.0};
    final double[] fullWeights = new double[] {1.0, 2.0, 1000.0};

    // Segment covering only the first two elements
    final int begin = 0;
    final int length = 2;
    final double[] segValues = new double[] {fullValues[0], fullValues[1]};
    final double[] segWeights = new double[] {fullWeights[0], fullWeights[1]};

    Assert.assertEquals(stat.evaluate(segValues, segWeights),
            stat.evaluate(fullValues, fullWeights, begin, length), 0);
}