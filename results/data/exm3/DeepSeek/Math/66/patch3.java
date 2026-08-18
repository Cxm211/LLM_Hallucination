    public double optimize(final UnivariateRealFunction f, final GoalType goalType, final double min, final double max) throws MaxIterationsExceededException, FunctionEvaluationException {
        return optimize(f, goalType, min, max, min + GOLDEN_SECTION * (max - min));
    }