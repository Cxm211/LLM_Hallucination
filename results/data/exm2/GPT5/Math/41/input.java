    public double evaluate(final double[] values, final double[] weights,
                           final double mean, final int begin, final int length) {

        double var = Double.NaN;

        if (test(values, weights, begin, length)) {
            if (length == 1) {
                var = 0.0;
            } else if (length > 1) {
                double accum = 0.0;
                double dev = 0.0;
                double accum2 = 0.0;
                for (int i = begin; i < begin + length; i++) {
                    dev = values[i] - mean;
                    accum += weights[i] * (dev * dev);
                    accum2 += weights[i] * dev;
                }

                double sumWts = 0;
                for (int i = 0; i < weights.length; i++) {
                    sumWts += weights[i];
                }

                if (isBiasCorrected) {
                    var = (accum - (accum2 * accum2 / sumWts)) / (sumWts - 1.0);
                } else {
                    var = (accum - (accum2 * accum2 / sumWts)) / sumWts;
                }
            }
        }
        return var;
    }

// trigger testcase
@Test
    public void testEvaluateArraySegmentWeighted() {
        // See if this statistic computes weighted statistics
        // If not, skip this test
        UnivariateStatistic statistic = getUnivariateStatistic();
        if (!(statistic instanceof WeightedEvaluation)) {
            return;
        }
        final WeightedEvaluation stat = (WeightedEvaluation) getUnivariateStatistic();
        final double[] arrayZero = new double[5];
        final double[] weightZero = new double[5];
        System.arraycopy(testArray, 0, arrayZero, 0, 5);
        System.arraycopy(testWeightsArray, 0, weightZero, 0, 5);
        Assert.assertEquals(stat.evaluate(arrayZero, weightZero),
                stat.evaluate(testArray, testWeightsArray, 0, 5), 0);
        final double[] arrayOne = new double[5];
        final double[] weightOne = new double[5];
        System.arraycopy(testArray, 5, arrayOne, 0, 5);
        System.arraycopy(testWeightsArray, 5, weightOne, 0, 5);
        Assert.assertEquals(stat.evaluate(arrayOne, weightOne),
                stat.evaluate(testArray, testWeightsArray, 5, 5), 0);
        final double[] arrayEnd = new double[5];
        final double[] weightEnd = new double[5];
        System.arraycopy(testArray, testArray.length - 5, arrayEnd, 0, 5);
        System.arraycopy(testWeightsArray, testArray.length - 5, weightEnd, 0, 5);
        Assert.assertEquals(stat.evaluate(arrayEnd, weightEnd),
                stat.evaluate(testArray, testWeightsArray, testArray.length - 5, 5), 0);
    }
