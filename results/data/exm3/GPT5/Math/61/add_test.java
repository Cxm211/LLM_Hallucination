// org/apache/commons/math/distribution/PoissonDistributionTest.java::testConstructorEpsilonMaxItersNegativeMean
public void testConstructorEpsilonMaxItersNegativeMean() {
        try {
            new PoissonDistributionImpl(-5.0, 1e-12, 1000000);
            fail("negative mean: NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            // Expected.
        }
    }