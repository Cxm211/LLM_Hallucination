// org/apache/commons/math3/optimization/fitting/HarmonicFitterTest.java
public void testTwoPoints() {
        final WeightedObservedPoint[] points = new WeightedObservedPoint[2];
        points[0] = new WeightedObservedPoint(1, 0.0, 0.0);
        points[1] = new WeightedObservedPoint(1, 1.0, 10.0);

        final HarmonicFitter.ParameterGuesser guesser
            = new HarmonicFitter.ParameterGuesser(points);
        double[] params = guesser.guess();
        for (double p : params) {
            assertFalse(Double.isNaN(p));
            assertFalse(Double.isInfinite(p));
        }
    }
