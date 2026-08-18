public double[] fit() {
    final double[] guess = (new ParameterGuesser(getObservations())).guess();
    return fit(new Gaussian.Parametric(), new double[] { guess[1], guess[0], guess[2] });
}