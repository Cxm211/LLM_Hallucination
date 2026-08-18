// org/apache/commons/math3/optimization/fitting/HarmonicFitterTest.java
public void testConstantY() {
        final double[] y = { 5.0, 5.0, 5.0, 5.0 };
        final int len = y.length;
        final WeightedObservedPoint[] points = new WeightedObservedPoint[len];
        for (int i = 0; i < len; i++) {
            points[i] = new WeightedObservedPoint(1, i, y[i]);
        }

        final HarmonicFitter.ParameterGuesser guesser
            = new HarmonicFitter.ParameterGuesser(points);
        double[] params = guesser.guess();
        for (double p : params) {
            assertFalse(Double.isNaN(p));
            assertFalse(Double.isInfinite(p));
        }
    }
