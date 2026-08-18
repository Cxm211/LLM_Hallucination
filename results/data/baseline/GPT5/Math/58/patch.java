public double[] fit() {
        final double[] guess;
        if (getObservations().size() < 3) {
            guess = new double[] {1, 0, 1};
        } else {
            guess = (new ParameterGuesser(getObservations())).guess();
        }
        return fit(new Gaussian.Parametric(), guess);
    }