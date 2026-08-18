// org/apache/commons/math3/optimization/fitting/HarmonicFitterTest.java
public void testLinearFunction() {
    final double[] y = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    final int len = y.length;
    final WeightedObservedPoint[] points = new WeightedObservedPoint[len];
    for (int i = 0; i < len; i++) {
        points[i] = new WeightedObservedPoint(1, i, y[i]);
    }

    final HarmonicFitter.ParameterGuesser guesser
        = new HarmonicFitter.ParameterGuesser(points);

    guesser.guess();
}