// buggy code
    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                    sampleSize);
        }

        final T[]out = (T[]) java.lang.reflect.Array.newInstance(singletons.get(0).getClass(), sampleSize);

        for (int i = 0; i < sampleSize; i++) {
            out[i] = sample();
        }

        return out;

    }

// relevant test
// org.apache.commons.math3.distribution.DiscreteIntegerDistributionTest::testExceptions
    public void testExceptions() {
        DiscreteIntegerDistribution invalid = null;
        try {
            invalid = new DiscreteIntegerDistribution(new int[]{1, 2}, new double[]{0.0});
            Assert.fail("Expected DimensionMismatchException");
        } catch (DimensionMismatchException e) {
        }
        try {
            invalid = new DiscreteIntegerDistribution(new int[]{1, 2}, new double[]{0.0, -1.0});
            Assert.fail("Expected NotPositiveException");
        } catch (NotPositiveException e) {
        }
        try {
            invalid = new DiscreteIntegerDistribution(new int[]{1, 2}, new double[]{0.0, 0.0});
            Assert.fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
        }
        try {
            invalid = new DiscreteIntegerDistribution(new int[]{1, 2}, new double[]{0.0, Double.NaN});
            Assert.fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
        }
        try {
            invalid = new DiscreteIntegerDistribution(new int[]{1, 2}, new double[]{0.0, Double.POSITIVE_INFINITY});
            Assert.fail("Expected MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
        }
        Assert.assertNull("Expected non-initialized DiscreteRealDistribution", invalid);
    }

// org.apache.commons.math3.distribution.DiscreteIntegerDistributionTest::testProbability
    public void testProbability() {
        int[] points = new int[]{-2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8};
        double[] results = new double[]{0, 0.2, 0, 0, 0, 0.5, 0, 0, 0, 0.3, 0};
        for (int p = 0; p < points.length; p++) {
            double probability = testDistribution.probability(points[p]);
            Assert.assertEquals(results[p], probability, 0.0);
        }
    }

// org.apache.commons.math3.distribution.DiscreteIntegerDistributionTest::testCumulativeProbability
    public void testCumulativeProbability() {
        int[] points = new int[]{-2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8};
        double[] results = new double[]{0, 0.2, 0.2, 0.2, 0.2, 0.7, 0.7, 0.7, 0.7, 1.0, 1.0};
        for (int p = 0; p < points.length; p++) {
            double probability = testDistribution.cumulativeProbability(points[p]);
            Assert.assertEquals(results[p], probability, 1e-10);
        }
    }

// org.apache.commons.math3.distribution.DiscreteIntegerDistributionTest::testGetNumericalMean
    public void testGetNumericalMean() {
        Assert.assertEquals(3.4, testDistribution.getNumericalMean(), 1e-10);
    }

// org.apache.commons.math3.distribution.DiscreteIntegerDistributionTest::testGetNumericalVariance
    public void testGetNumericalVariance() {
        Assert.assertEquals(7.84, testDistribution.getNumericalVariance(), 1e-10);
    }

// org.apache.commons.math3.distribution.DiscreteIntegerDistributionTest::testGetSupportLowerBound
    public void testGetSupportLowerBound() {
        Assert.assertEquals(-1, testDistribution.getSupportLowerBound());
    }

// org.apache.commons.math3.distribution.DiscreteIntegerDistributionTest::testGetSupportUpperBound
    public void testGetSupportUpperBound() {
        Assert.assertEquals(7, testDistribution.getSupportUpperBound());
    }

// org.apache.commons.math3.distribution.DiscreteIntegerDistributionTest::testIsSupportConnected
    public void testIsSupportConnected() {
        Assert.assertTrue(testDistribution.isSupportConnected());
    }

// org.apache.commons.math3.distribution.DiscreteIntegerDistributionTest::testSample
    public void testSample() {
        final int n = 1000000;
        testDistribution.reseedRandomGenerator(-334759360); 
        final int[] samples = testDistribution.sample(n);
        Assert.assertEquals(n, samples.length);
        double sum = 0;
        double sumOfSquares = 0;
        for (int i = 0; i < samples.length; i++) {
            sum += samples[i];
            sumOfSquares += samples[i] * samples[i];
        }
        Assert.assertEquals(testDistribution.getNumericalMean(),
                sum / n, 1e-2);
        Assert.assertEquals(testDistribution.getNumericalVariance(),
                sumOfSquares / n - FastMath.pow(sum / n, 2), 1e-2);
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testExceptions
    public void testExceptions() {
        DiscreteRealDistribution invalid = null;
        try {
            invalid = new DiscreteRealDistribution(new double[]{1.0, 2.0}, new double[]{0.0});
            Assert.fail("Expected DimensionMismatchException");
        } catch (DimensionMismatchException e) {
        }
        try{
        invalid = new DiscreteRealDistribution(new double[]{1.0, 2.0}, new double[]{0.0, -1.0});
            Assert.fail("Expected NotPositiveException");
        } catch (NotPositiveException e) {
        }
        try {
            invalid = new DiscreteRealDistribution(new double[]{1.0, 2.0}, new double[]{0.0, 0.0});
            Assert.fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
        }
        try {
            invalid = new DiscreteRealDistribution(new double[]{1.0, 2.0}, new double[]{0.0, Double.NaN});
            Assert.fail("Expected MathArithmeticException");
        } catch (MathArithmeticException e) {
        }
        try {
            invalid = new DiscreteRealDistribution(new double[]{1.0, 2.0}, new double[]{0.0, Double.POSITIVE_INFINITY});
            Assert.fail("Expected MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
        }
        Assert.assertNull("Expected non-initialized DiscreteRealDistribution", invalid);
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testProbability
    public void testProbability() {
        double[] points = new double[]{-2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
        double[] results = new double[]{0, 0.2, 0, 0, 0, 0.5, 0, 0, 0, 0.3, 0};
        for (int p = 0; p < points.length; p++) {
            double density = testDistribution.probability(points[p]);
            Assert.assertEquals(results[p], density, 0.0);
        }
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testDensity
    public void testDensity() {
        double[] points = new double[]{-2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
        double[] results = new double[]{0, 0.2, 0, 0, 0, 0.5, 0, 0, 0, 0.3, 0};
        for (int p = 0; p < points.length; p++) {
            double density = testDistribution.density(points[p]);
            Assert.assertEquals(results[p], density, 0.0);
        }
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testCumulativeProbability
    public void testCumulativeProbability() {
        double[] points = new double[]{-2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
        double[] results = new double[]{0, 0.2, 0.2, 0.2, 0.2, 0.7, 0.7, 0.7, 0.7, 1.0, 1.0};
        for (int p = 0; p < points.length; p++) {
            double probability = testDistribution.cumulativeProbability(points[p]);
            Assert.assertEquals(results[p], probability, 1e-10);
        }
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testGetNumericalMean
    public void testGetNumericalMean() {
        Assert.assertEquals(3.4, testDistribution.getNumericalMean(), 1e-10);
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testGetNumericalVariance
    public void testGetNumericalVariance() {
        Assert.assertEquals(7.84, testDistribution.getNumericalVariance(), 1e-10);
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testGetSupportLowerBound
    public void testGetSupportLowerBound() {
        Assert.assertEquals(-1, testDistribution.getSupportLowerBound(), 0);
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testGetSupportUpperBound
    public void testGetSupportUpperBound() {
        Assert.assertEquals(7, testDistribution.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testIsSupportLowerBoundInclusive
    public void testIsSupportLowerBoundInclusive() {
        Assert.assertTrue(testDistribution.isSupportLowerBoundInclusive());
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testIsSupportUpperBoundInclusive
    public void testIsSupportUpperBoundInclusive() {
        Assert.assertTrue(testDistribution.isSupportUpperBoundInclusive());
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testIsSupportConnected
    public void testIsSupportConnected() {
        Assert.assertTrue(testDistribution.isSupportConnected());
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testSample
    public void testSample() {
        final int n = 1000000;
        testDistribution.reseedRandomGenerator(-334759360); 
        final double[] samples = testDistribution.sample(n);
        Assert.assertEquals(n, samples.length);
        double sum = 0;
        double sumOfSquares = 0;
        for (int i = 0; i < samples.length; i++) {
            sum += samples[i];
            sumOfSquares += samples[i] * samples[i];
        }
        Assert.assertEquals(testDistribution.getNumericalMean(),
                sum / n, 1e-2);
        Assert.assertEquals(testDistribution.getNumericalVariance(),
                sumOfSquares / n - FastMath.pow(sum / n, 2), 1e-2);
    }

// org.apache.commons.math3.distribution.DiscreteRealDistributionTest::testIssue942
    public void testIssue942() {
        List<Pair<Object,Double>> list = new ArrayList<Pair<Object, Double>>();
        list.add(new Pair<Object, Double>(new Object() {}, new Double(0)));
        list.add(new Pair<Object, Double>(new Object() {}, new Double(1)));
        Assert.assertEquals(1, new DiscreteDistribution<Object>(list).sample(1).length);
    }
