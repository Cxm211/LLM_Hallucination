// org/apache/commons/math3/distribution/RealDistributionAbstractTest.java
@Test
    public void testIsSupportLowerBoundInclusive_NaN_Density() {
        final RealDistribution dist = new org.apache.commons.math3.distribution.AbstractRealDistribution(new org.apache.commons.math3.random.RandomAdaptor(new java.util.Random(0))) {
            @Override
            public double density(final double x) { return Double.NaN; }
            @Override
            public double cumulativeProbability(final double x) { return 0.0; }
            @Override
            public double getNumericalMean() { return 0.0; }
            @Override
            public double getNumericalVariance() { return 0.0; }
            @Override
            public double getSupportLowerBound() { return 0.0; }
            @Override
            public double getSupportUpperBound() { return 1.0; }
        };
        Assert.assertFalse(dist.isSupportLowerBoundInclusive());
    }
