public UnivariateRealPointValuePair optimize(final FUNC f,
                                             final GoalType goal,
                                             final double min, final double max)
    throws FunctionEvaluationException {
    final double start = 0.5 * (min + max);
    return optimize(f, goal, min, max, start);
}