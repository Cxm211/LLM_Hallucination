// org/apache/commons/math/optimization/fitting/GaussianFitterTest.java::testMath519Variant
@Test
public void testMath519Variant() {
    // Similar tail-only data but with different norm to exercise a different path.
    final double norm = 2.0;
    final double mean = 53.1572792;
    final double sigma = 5.75214622;

    GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
    for (int i = 0; i <= 26; i++) {
        double y = norm * Math.exp(-0.5 * Math.pow((i - mean) / sigma, 2.0));
        fitter.addObservedPoint(i, y);
    }
    final double[] p = fitter.fit();

    Assert.assertEquals(mean, p[1], 1e-6);
    Assert.assertEquals(sigma, p[2], 1e-6);
}
