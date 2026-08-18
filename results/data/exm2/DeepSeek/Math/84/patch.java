protected void iterateSimplex(final Comparator<RealPointValuePair> comparator)
        throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {

        incrementIterationsCounter();

        // save the original vertex
        final RealPointValuePair[] original = simplex;
        final RealPointValuePair best = original[0];

        // perform a reflection step
        final RealPointValuePair reflected = evaluateNewSimplex(original, 1.0, comparator);
        if (comparator.compare(reflected, best) < 0) {

            // compute the expanded simplex
            final RealPointValuePair[] reflectedSimplex = simplex;
            final RealPointValuePair expanded = evaluateNewSimplex(original, khi, comparator);
            if (comparator.compare(reflected, expanded) <= 0) {
                // accept the reflected simplex
                simplex = reflectedSimplex;
            }

            return;

        }

        // compute the contracted simplex
        final RealPointValuePair contracted = evaluateNewSimplex(original, gamma, comparator);
        if (comparator.compare(contracted, best) < 0) {
            // accept the contracted simplex
            return;
        }

        // check convergence
        if (checkConvergence()) {
            return;
        }

        // neither reflection nor contraction were successful:
        // perform a shrink step
        final double[][] points = new double[simplex.length][];
        for (int i = 0; i < simplex.length; ++i) {
            points[i] = simplex[i].getPointRef();
        }
        final double[] bestPoint = points[0];
        for (int i = 1; i < points.length; ++i) {
            for (int j = 0; j < bestPoint.length; ++j) {
                points[i][j] = bestPoint[j] + sigma * (points[i][j] - bestPoint[j]);
            }
            simplex[i] = new RealPointValuePair(points[i], Double.NaN, false);
        }
        evaluateSimplex(comparator);
    }