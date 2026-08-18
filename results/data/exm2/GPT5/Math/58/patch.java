    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit(new ParametricUnivariateFunction() {
            private final Gaussian.Parametric f = new Gaussian.Parametric();
            public double value(double x, double... p) {
                try {
                    return f.value(x, p);
                } catch (Exception e) {
                    return Double.NaN;
                }
            }
            public double[] gradient(double x, double... p) {
                try {
                    return f.gradient(x, p);
                } catch (Exception e) {
                    return new double[] { Double.NaN, Double.NaN, Double.NaN };
                }
            }
        }, guess);
    }