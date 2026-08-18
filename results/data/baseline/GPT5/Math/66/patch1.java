protected double doOptimize()
        throws MaxIterationsExceededException, FunctionEvaluationException {
        return localMin(getGoalType() == GoalType.MINIMIZE,
                        getFunction(), getGoalType(), getMin(), getStartValue(), getMax(),
                        getRelativeAccuracy(), getAbsoluteAccuracy());
    }