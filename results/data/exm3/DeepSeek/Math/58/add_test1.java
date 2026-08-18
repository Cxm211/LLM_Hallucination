// org/apache/commons/math/optimization/fitting/GaussianFitterTest.java
@Test
    public void testMath519Additional2() {
        // Gaussian with very small amplitude and shifted mean.
        final double[] data = new double[30];
        for (int i = 0; i < data.length; i++) {
            double x = i;
            // Gaussian with norm=1e-30, mean=10, sigma=2
            data[i] = 1e-30 * Math.exp(-(x-10)*(x-10)/(2*4));
        }
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        for (int i = 0; i < data.length; i++) {
            fitter.addObservedPoint(i, data[i]);
        }
        final double[] p = fitter.fit();
        Assert.assertEquals(10.0, p[1], 1e-7);
        Assert.assertEquals(2.0, p[2], 1e-7);
    }
