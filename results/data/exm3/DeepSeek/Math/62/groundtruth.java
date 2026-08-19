// ===== FIXED org.apache.commons.math.optimization.univariate.MultiStartUnivariateRealOptimizer :: optimize(FUNC, GoalType, double, double) [lines 142-147] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-62-fixed/src/main/java/org/apache/commons/math/optimization/univariate/MultiStartUnivariateRealOptimizer.java =====
    public UnivariateRealPointValuePair optimize(final FUNC f,
                                                 final GoalType goal,
                                                 final double min, final double max)
        throws FunctionEvaluationException {
        return optimize(f, goal, min, max, min + 0.5 * (max - min));
    }

// ===== FIXED org.apache.commons.math.optimization.univariate.MultiStartUnivariateRealOptimizer :: optimize(FUNC, GoalType, double, double, double) [lines 150-182] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Math/Math-62-fixed/src/main/java/org/apache/commons/math/optimization/univariate/MultiStartUnivariateRealOptimizer.java =====
    public UnivariateRealPointValuePair optimize(final FUNC f, final GoalType goal,
                                                 final double min, final double max,
                                                 final double startValue)
        throws FunctionEvaluationException {
        optima = new UnivariateRealPointValuePair[starts];
        totalEvaluations = 0;

        // Multi-start loop.
        for (int i = 0; i < starts; ++i) {
            try {
                final double s = (i == 0) ? startValue : min + generator.nextDouble() * (max - min);
                optima[i] = optimizer.optimize(f, goal, min, max, s);
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
