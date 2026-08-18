// org/apache/commons/math/stat/descriptive/UnivariateStatisticAbstractTest.java
@Test
    public void testEvaluateArraySegmentWeightedBug() {
        for (boolean biasCorrected : new boolean[] {true, false}) {
            org.apache.commons.math.stat.descriptive.moment.Variance variance = new org.apache.commons.math.stat.descriptive.moment.Variance(biasCorrected);
            if (!(variance instanceof org.apache.commons.math.stat.descriptive.WeightedEvaluation)) {
                continue;
            }
            org.apache.commons.math.stat.descriptive.WeightedEvaluation stat = (org.apache.commons.math.stat.descriptive.WeightedEvaluation) variance;
            final double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
            final double[] weights = {0.5, 1.0, 1.5, 2.0, 2.5, 3.0};
            final int begin = 1;
            final int length = 3;
            final double[] segmentValues = new double[length];
            final double[] segmentWeights = new double[length];
            System.arraycopy(values, begin, segmentValues, 0, length);
            System.arraycopy(weights, begin, segmentWeights, 0, length);
            final double expected = stat.evaluate(segmentValues, segmentWeights);
            final double actual = stat.evaluate(values, weights, begin, length);
            org.junit.Assert.assertEquals(expected, actual, 1e-14);
        }
    }
