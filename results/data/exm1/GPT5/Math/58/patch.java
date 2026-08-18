public double[] fit() {
        final java.util.Collection<?> obs = getObservations();
        if (obs == null || obs.size() < 3) {
            throw new IllegalArgumentException("At least 3 observations are required");
        }
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit(new Gaussian.Parametric(), guess);
    }