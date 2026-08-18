// org/apache/commons/math3/fitting/PolynomialFitterTest.java
@Test
    public void testZeroWeight() {
        PolynomialFunction f = new PolynomialFunction(new double[] {2, 3});
        PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
        for (int i = 0; i < 10; i++) {
            double x = -1.0 + i * 0.2;
            double y = f.value(x);
            fitter.addObservedPoint(1.0, x, y);
        }
        // Add an outlier with zero weight
        fitter.addObservedPoint(0.0, 5.0, 100.0);
        double[] init = new double[2];
        PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));
        Assert.assertEquals(2.0, fitted.getCoefficients()[0], 1e-10);
        Assert.assertEquals(3.0, fitted.getCoefficients()[1], 1e-10);
    }
