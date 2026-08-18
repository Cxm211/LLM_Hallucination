public UnivariateRealPointValuePair optimize(final FUNC f, final GoalType goal,
                                                 final double min, final double max,
                                                 final double startValue)
        throws FunctionEvaluationException {
        optima = new UnivariateRealPointValuePair[starts];
        totalEvaluations = 0;

        final int initialMaxEvaluations = optimizer.getMaxEvaluations();

        // Multi-start loop.
        for (int i = 0; i < starts; ++i) {
            try {
                // Ensure remaining evaluations are properly set before each start.
                optimizer.setMaxEvaluations(initialMaxEvaluations - totalEvaluations);
                final int evalsBefore = optimizer.getEvaluations();

                final double bound1 = (i == 0) ? min : min + generator.nextDouble() * (max - min);
                final double bound2 = (i == 0) ? max : min + generator.nextDouble() * (max - min);
                optima[i] = optimizer.optimize(f, goal, FastMath.min(bound1, bound2), FastMath.max(bound1, bound2));

                final int evalsAfter = optimizer.getEvaluations();
                final int usedEvaluations = evalsAfter - evalsBefore;
                totalEvaluations += usedEvaluations;
            } catch (FunctionEvaluationException fee) {
                optima[i] = null;
            } catch (ConvergenceException ce) {
                optima[i] = null;
            }
        }

        sortPairs(goal);

        if (optima[0] == null) {
            throw new ConvergenceException(LocalizedFormats.NO_CONVERGENCE_WITH_ANY_START_POINT,
                                           starts);
        }

        // Return the point with the best objective function value.
        return optima[0];
    }