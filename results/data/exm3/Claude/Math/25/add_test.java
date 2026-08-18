// org/apache/commons/math3/optimization/fitting/HarmonicFitterTest.java
public void testConstantFunction() {
    final double[] y = { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 };
    final int len = y.length;
    final WeightedObservedPoint[] points = new WeightedObservedPoint[len];
    for (int i = 0; i < len; i++) {
        points[i] = new WeightedObservedPoint(1, i, y[i]);
    }

    final HarmonicFitter.ParameterGuesser guesser
        = new HarmonicFitter.ParameterGuesser(points);

    guesser.guess();
}