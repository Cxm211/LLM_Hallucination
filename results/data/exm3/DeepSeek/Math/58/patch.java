    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        // Ensure that sigma is positive.
        guess[2] = Math.abs(guess[2]);
        return fit(new Gaussian.Parametric(), guess);
    }