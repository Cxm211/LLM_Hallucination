// org/apache/commons/math3/distribution/RealDistributionAbstractTest.java
@Test
    public void testIsSupportLowerBoundInclusive_InfiniteBound() {
        final RealDistribution dist = new org.apache.commons.math3.distribution.AbstractRealDistribution(new org.apache.commons.math3.random.RandomAdaptor(new java.util.Random(0))) {
            @Override
            public double density(final double x) { return 0.0; }
            @Override
            public double cumulativeProbability(final double x) { return 0.0; }
            @Override
            public double getNumericalMean() { return 0.0; }
            @Override
            public double getNumericalVariance() { return 0.0; }
            @Override
            public double getSupportLowerBound() { return Double.NEGATIVE_INFINITY; }
            @Override
            public double getSupportUpperBound() { return Double.POSITIVE_INFINITY; }
        };
        Assert.assertFalse(dist.isSupportLowerBoundInclusive());
    }
