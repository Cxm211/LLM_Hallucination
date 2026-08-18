// org/apache/commons/math/optimization/fitting/GaussianFitterTest.java
@Test
    public void testMath519Additional1() {
        // Data with extremely small values, may cause negative sigma guess.
        final double[] data = {
            1e-100, 2e-100, 5e-100, 1e-99, 5e-99, 1e-98, 5e-98, 1e-97, 5e-97, 1e-96,
            5e-96, 1e-95, 5e-95, 1e-94, 5e-94, 1e-93, 5e-93, 1e-92, 5e-92, 1e-91,
            5e-91, 1e-90, 5e-90, 1e-89, 5e-89, 1e-88, 5e-88, 1e-87
        };
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        for (int i = 0; i < data.length; i++) {
            fitter.addObservedPoint(i, data[i]);
        }
        final double[] p = fitter.fit();
        Assert.assertTrue(p[2] > 0);
        Assert.assertFalse(Double.isNaN(p[0]));
        Assert.assertFalse(Double.isNaN(p[1]));
        Assert.assertFalse(Double.isNaN(p[2]));
    }
