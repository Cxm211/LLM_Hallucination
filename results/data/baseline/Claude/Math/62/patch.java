public UnivariateRealPointValuePair optimize(final FUNC f,
                                                 final GoalType goal,
                                                 final double min, final double max)
        throws FunctionEvaluationException {
        return optimize(f, goal, min, max, min + generator.nextDouble() * (max - min));
    }