// org/apache/commons/math3/optimization/fitting/PolynomialFitterTest.java
@Test
public void testMediumSampleLowDegree() {
    Random randomizer = new Random(0xfedcba9876543210l);
    int degree = 2;
    PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

    PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
    for (int i = 0; i < 1000; ++i) {
        double x = -1.0 + i / 500.0;
        fitter.addObservedPoint(1.0, x, p.value(x) + 0.2 * randomizer.nextGaussian());
    }

    final double[] init = new double[degree + 1];
    PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

    for (double x = -1.0; x < 1.0; x += 0.05) {
        double error = FastMath.abs(p.value(x) - fitted.value(x)) / (1.0 + FastMath.abs(p.value(x)));
        Assert.assertTrue(FastMath.abs(error) < 0.05);
    }
}