    public double[] guessParametersErrors(EstimationProblem problem)
      throws EstimationException {
        int m = problem.getMeasurements().length;
        int p = problem.getUnboundParameters().length;
        if (m <= p) {
            throw new EstimationException("no degrees of freedom ({0} measurements, {1} parameters)",
                                          new Object[] { new Integer(m), new Integer(p)});
        }
        double[] errors = new double[problem.getAllParameters().length];
        final double c = Math.sqrt(getChiSquare(problem) / (m - p));
        double[][] covar = getCovariances(problem);
        EstimatedParameter[] allParams = problem.getAllParameters();
        EstimatedParameter[] unboundParams = problem.getUnboundParameters();
        for (int i = 0; i < allParams.length; ++i) {
            if (allParams[i].isBound()) {
                errors[i] = 0.0;
            } else {
                // find index in unboundParams
                int uIndex = -1;
                for (int j = 0; j < unboundParams.length; ++j) {
                    if (unboundParams[j] == allParams[i]) {
                        uIndex = j;
                        break;
                    }
                }
                errors[i] = Math.sqrt(covar[uIndex][uIndex]) * c;
            }
        }
        return errors;
    }