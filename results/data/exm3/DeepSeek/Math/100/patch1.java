    public double[] guessParametersErrors(EstimationProblem problem)
      throws EstimationException {
        int m = problem.getMeasurements().length;
        EstimatedParameter[] unbound = problem.getUnboundParameters();
        int p = unbound.length;
        if (m <= p) {
            throw new EstimationException("no degrees of freedom ({0} measurements, {1} parameters)",
                                          new Object[] { new Integer(m), new Integer(p)});
        }
        double[] errors = new double[p];
        final double c = Math.sqrt(getChiSquare(problem) / (m - p));
        double[][] covar = getCovariances(problem);
        for (int i = 0; i < p; ++i) {
            errors[i] = Math.sqrt(covar[i][i]) * c;
        }
        return errors;
    }