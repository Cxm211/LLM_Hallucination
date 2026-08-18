// org/apache/commons/math3/optimization/fitting/PolynomialFitterTest.java::testLargeSample
@Test
public void testLargeSampleVaryingWeights() {
    Random randomizer = new Random(0x123456789ABCDEFL);
    // Use a moderate degree but lots of points with varying positive weights
    int degree = 5;
    PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

    PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
    for (int i = 0; i < 40000; ++i) {
        double x = -1.0 + i / 20000.0;
        double w = 0.5 + (i % 7); // weights in {0.5, 1.5, ..., 6.5}
        fitter.addObservedPoint(w, x, p.value(x) + 0.1 * randomizer.nextGaussian());
    }

    final double[] init = new double[degree + 1];
    PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

    for (double x = -1.0; x < 1.0; x += 0.02) {
        double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                       (1.0 + FastMath.abs(p.value(x)));
        Assert.assertTrue(FastMath.abs(error) < 0.02);
    }
}