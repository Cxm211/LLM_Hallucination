protected PointValuePair doOptimize() {
    checkParameters();

    final MultivariateFunction evalFunc
        = new MultivariateFunction() {
            public double value(double[] point) {
                return computeObjectiveValue(point);
            }
        };

    final boolean isMinim = getGoalType() == GoalType.MINIMIZE;
    final Comparator<PointValuePair> comparator
        = new Comparator<PointValuePair>() {
        public int compare(final PointValuePair o1,
                           final PointValuePair o2) {
            final double v1 = o1.getValue();
            final double v2 = o2.getValue();
            return isMinim ? Double.compare(v1, v2) : Double.compare(v2, v1);
        }
    };

    simplex.build(getStartPoint());
    simplex.evaluate(evalFunc, comparator);

    PointValuePair[] previous = null;
    int iteration = 0;
    final ConvergenceChecker<PointValuePair> checker = getConvergenceChecker();
    while (true) {
        if (iteration > 0) {
            boolean converged = true;
            for (int i = 0; i < simplex.getSize(); i++) {
                PointValuePair prev = previous[i];
                converged = converged &&
                    checker.converged(iteration, prev, simplex.getPoint(i));
            }
            if (converged) {
                return simplex.getPoint(0);
            }
        }

        previous = simplex.getPoints();
        simplex.iterate(evalFunc, comparator);
        ++iteration;
    }
}