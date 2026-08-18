// org/apache/commons/math3/optimization/fitting/HarmonicFitterTest.java::testMath844DegenerateTwoPoints
public void testMath844DegenerateTwoPoints() {
        final double[] y = {0.0, 1.0};
        final WeightedObservedPoint[] points = new WeightedObservedPoint[y.length];
        for (int i = 0; i < y.length; i++) {
            points[i] = new WeightedObservedPoint(1, i, y[i]);
        }
        final HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        guesser.guess();
    }