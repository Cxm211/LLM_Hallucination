// org/apache/commons/math/distribution/PoissonDistributionTest.java
public void testMeanZero() {
        try {
            new PoissonDistributionImpl(0.0, 1e-12, 1000000);
            fail("zero mean: NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            // Expected.
        }
    }
