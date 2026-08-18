    public double optimize(final UnivariateRealFunction f, final GoalType goalType, final double min, final double max, final double startValue) throws MaxIterationsExceededException, FunctionEvaluationException {
        setGoalType(goalType);
        clearResult();
        return localMin(getGoalType() == GoalType.MINIMIZE,
                        f, goalType, min, startValue, max,
                        getRelativeAccuracy(), getAbsoluteAccuracy());
    }