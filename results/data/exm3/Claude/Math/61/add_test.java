// org/apache/commons/math/distribution/PoissonDistributionTest.java
public void testZeroMean() {
    try {
        PoissonDistribution dist = new PoissonDistributionImpl(0);
        fail("zero mean: NotStrictlyPositiveException expected");
    } catch(NotStrictlyPositiveException ex) {
        // Expected.
    }
}