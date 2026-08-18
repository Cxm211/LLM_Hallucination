// org/apache/commons/math/optimization/fitting/GaussianFitterTest.java
@Test
public void testMath519_widerPeak() {
    // Test with a wider peak (larger sigma)
    final double[] data = {
        1e-10, 5e-10, 2e-9, 8e-9, 3e-8,
        1e-7, 3e-7, 8e-7, 2e-6, 4e-6,
        8e-6, 1e-5, 8e-6, 4e-6, 2e-6,
        8e-7, 3e-7, 1e-7, 3e-8, 8e-9,
        2e-9, 5e-10, 1e-10
    };
    
    GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
    for (int i = 0; i < data.length; i++) {
        fitter.addObservedPoint(i, data[i]);
    }
    final double[] p = fitter.fit();
    
    // Verify mean is near center and sigma is positive and reasonably large
    Assert.assertTrue("Mean should be near center", Math.abs(p[1] - 11.0) < 3.0);
    Assert.assertTrue("Sigma must be positive", p[2] > 0);
    Assert.assertTrue("Sigma should be reasonable", p[2] < 20.0);
}