// org/apache/commons/math/distribution/PoissonDistributionTest.java
public void testVerySmallPositiveMean() {
    try {
        PoissonDistribution dist = new PoissonDistributionImpl(0.0001);
        fail("very small positive mean: NotStrictlyPositiveException expected");
    } catch(NotStrictlyPositiveException ex) {
        // Expected.
    }
}