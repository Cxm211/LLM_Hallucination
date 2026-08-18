    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        // Ensure sigma (third parameter) is positive
        if (guess[2] <= 0) {
            guess[2] = -guess[2];
        }
        return fit(new Gaussian.Parametric(), guess);
    }