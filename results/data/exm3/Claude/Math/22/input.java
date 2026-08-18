// buggy function
    public boolean isSupportLowerBoundInclusive() {
        return true;
    }

    public boolean isSupportUpperBoundInclusive() {
        return false;
    }

// trigger testcase
// org/apache/commons/math3/distribution/RealDistributionAbstractTest.java::testIsSupportLowerBoundInclusive
@Test
    public void testIsSupportLowerBoundInclusive() {
        final double lowerBound = distribution.getSupportLowerBound();
        double result = Double.NaN;
        result = distribution.density(lowerBound);
        Assert.assertEquals(
                !Double.isInfinite(lowerBound) && !Double.isNaN(result) &&
                !Double.isInfinite(result),
                distribution.isSupportLowerBoundInclusive());
         
    }

// org/apache/commons/math3/distribution/RealDistributionAbstractTest.java::testIsSupportUpperBoundInclusive
@Test
    public void testIsSupportUpperBoundInclusive() {
        final double upperBound = distribution.getSupportUpperBound();
        double result = Double.NaN;
        result = distribution.density(upperBound);
        Assert.assertEquals(
                !Double.isInfinite(upperBound) && !Double.isNaN(result) &&
                !Double.isInfinite(result),
                distribution.isSupportUpperBoundInclusive());
         
    }
