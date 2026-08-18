public double[] fit() {
    final WeightedObservedPoint[] observations = getObservations();
    if (observations == null || observations.length < 3) {
        throw new IllegalArgumentException("Not enough observations to fit a Gaussian (need at least 3)");
    }
    final double[] guess = (new ParameterGuesser(observations)).guess();
    return fit(new Gaussian.Parametric(), guess);
}