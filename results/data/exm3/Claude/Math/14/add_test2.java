// org/apache/commons/math3/fitting/PolynomialFitterTest.java
@Test
public void testVerySmallWeights() {
    PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
    fitter.addObservedPoint(1e-10, 0.0, 1.0);
    fitter.addObservedPoint(1e-10, 1.0, 2.0);
    fitter.addObservedPoint(1.0, 2.0, 3.0);
    double[] result = fitter.fit(new double[]{0.0, 0.0});
    Assert.assertNotNull(result);
}