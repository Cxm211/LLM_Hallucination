public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        final Gaussian.Parametric safe = new Gaussian.Parametric() {
            @Override
            public double value(double x, double... p) {
                try {
                    return super.value(x, p);
                } catch (org.apache.commons.math.exception.NotStrictlyPositiveException e) {
                    return Double.NaN;
                }
            }
            @Override
            public double[] gradient(double x, double... p) {
                try {
                    return super.gradient(x, p);
                } catch (org.apache.commons.math.exception.NotStrictlyPositiveException e) {
                    return new double[] { Double.NaN, Double.NaN, Double.NaN };
                }
            }
        };
        return fit(safe, guess);
    }