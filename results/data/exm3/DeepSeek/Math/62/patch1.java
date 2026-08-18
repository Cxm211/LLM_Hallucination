    public UnivariateRealPointValuePair optimize(final FUNC f, final GoalType goal,
                                                 final double min, final double max,
                                                 final double startValue)
        throws FunctionEvaluationException {
        optima = new UnivariateRealPointValuePair[starts];
        totalEvaluations = 0;

        // Multi-start loop.
        for (int i = 0; i < starts; ++i) {
            try {
                if (i == 0) {
                    // Use the provided startValue with the original bounds
                    optima[i] = optimizer.optimize(f, goal, min, max, startValue);
                } else {
                    // Generate random bounds and a random start value within those bounds
                    final double bound1 = min + generator.nextDouble() * (max - min);
                    final double bound2 = min + generator.nextDouble() * (max - min);
                    final double lower = FastMath.min(bound1, bound2);
                    final double upper = FastMath.max(bound1, bound2);
                    final double startVal = lower + generator.nextDouble() * (upper - lower);
                    optima[i] = optimizer.optimize(f, goal, lower, upper, startVal);
                }
            } catch (FunctionEvaluationException fee) {
                optima[i] = null;
            } catch (ConvergenceException ce) {
                optima[i] = null;
            }

            final int usedEvaluations = optimizer.getEvaluations();
            optimizer.setMaxEvaluations(optimizer.getMaxEvaluations() - usedEvaluations);
            totalEvaluations += usedEvaluations;
        }

        sortPairs(goal);

        if (optima[0] == null) {
            throw new ConvergenceException(LocalizedFormats.NO_CONVERGENCE_WITH_ANY_START_POINT,
                                           starts);
        }

        // Return the point with the best objective function value.
        return optima[0];
    }