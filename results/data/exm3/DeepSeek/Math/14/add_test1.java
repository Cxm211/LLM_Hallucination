// org/apache/commons/math3/fitting/PolynomialFitterTest.java
@Test
    public void testTinyWeight() {
        PolynomialFunction f = new PolynomialFunction(new double[] {1});
        PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
        double tiny = 1e-15;
        for (int i = 0; i < 5; i++) {
            double x = i;
            double y = f.value(x);
            fitter.addObservedPoint(tiny, x, y);
        }
        double[] init = new double[1];
        PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));
        Assert.assertEquals(1.0, fitted.getCoefficients()[0], 1e-10);
    }
