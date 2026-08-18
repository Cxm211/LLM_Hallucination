    public double solve(final UnivariateRealFunction f, double min, double max, double initial)
        throws MaxIterationsExceededException, FunctionEvaluationException {
        setFunction(f);
        return solve(min, max);
    }