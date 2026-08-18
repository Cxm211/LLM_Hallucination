// org/apache/commons/math3/fitting/PolynomialFitterTest.java
@Test(expected = IllegalArgumentException.class)
public void testNegativeWeight() {
    PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
    fitter.addObservedPoint(-1.0, 0.0, 1.0);
    fitter.fit(new double[]{0.0, 0.0});
}