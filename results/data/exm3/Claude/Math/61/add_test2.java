// org/apache/commons/math/distribution/PoissonDistributionTest.java
public void testLargeNegativeMean() {
    try {
        PoissonDistribution dist = new PoissonDistributionImpl(-100.0);
        fail("large negative mean: NotStrictlyPositiveException expected");
    } catch(NotStrictlyPositiveException ex) {
        // Expected.
    }
}