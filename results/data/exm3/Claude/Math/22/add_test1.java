// org/apache/commons/math3/distribution/RealDistributionAbstractTest.java
@Test
public void testIsSupportUpperBoundInclusiveWithInfiniteBound() {
    if (Double.isInfinite(distribution.getSupportUpperBound())) {
        Assert.assertFalse("Upper bound should not be inclusive when infinite",
                distribution.isSupportUpperBoundInclusive());
    }
}