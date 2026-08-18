// org/apache/commons/math/optimization/fitting/GaussianFitterTest.java
@Test
public void testMath519_symmetricData() {
    // Test with symmetric Gaussian data around a different center
    final double[] data = {
        1e-20, 5e-19, 1e-17, 1e-16, 5e-16,
        1e-15, 3e-15, 5e-15, 3e-15, 1e-15,
        5e-16, 1e-16, 1e-17, 5e-19, 1e-20
    };
    
    GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
    for (int i = 0; i < data.length; i++) {
        fitter.addObservedPoint(i, data[i]);
    }
    final double[] p = fitter.fit();
    
    // Verify mean is near center and sigma is positive
    Assert.assertTrue("Mean should be near center", Math.abs(p[1] - 7.0) < 2.0);
    Assert.assertTrue("Sigma must be positive", p[2] > 0);
}