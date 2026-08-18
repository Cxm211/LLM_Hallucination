// org/apache/commons/math3/distribution/RealDistributionAbstractTest.java
@Test
public void testIsSupportLowerBoundInclusiveWithInfiniteBound() {
    if (Double.isInfinite(distribution.getSupportLowerBound())) {
        Assert.assertFalse("Lower bound should not be inclusive when infinite",
                distribution.isSupportLowerBoundInclusive());
    }
}