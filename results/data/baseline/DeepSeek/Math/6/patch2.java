protected PointValuePair doOptimize() {
     isMinimize = getGoalType().equals(GoalType.MINIMIZE);
    final FitnessFunction fitfun = new FitnessFunction();
    final double[] guess = getStartPoint();
    dimension = guess.length;
    initializeCMA(guess);
    iterations = 0;
    double bestValue = fitfun.value(guess);
    push(fitnessHistory, bestValue);
    PointValuePair optimum
        = new PointValuePair(getStartPoint(),
                             isMinimize ? bestValue : -bestValue);
    PointValuePair lastResult = null;

    generationLoop:
    for (generations = 1; generations <= maxGenerations; generations++) {
        final RealMatrix arz = randn1(dimension, lambda);
        final RealMatrix arx = zeros(dimension, lambda);
        final double[] fitness = new double[lambda];
        for (int k = 0; k < lambda; k++) {
            RealMatrix arxk = null;
            for (int i = 0; i < checkFeasableCount + 1; i++) {
                if (diagonalOnly <= 0) {
                    arxk = xmean.add(BD.multiply(arz.getColumnMatrix(k))
                                     .scalarMultiply(sigma));
                } else {
                    arxk = xmean.add(times(diagD,arz.getColumnMatrix(k))
                                     .scalarMultiply(sigma));
                }
                if (i >= checkFeasableCount ||
                    fitfun.isFeasible(arxk.getColumn(0))) {
                    break;
                }
                arz.setColumn(k, randn(dimension));
            }
            copyColumn(arxk, 0, arx, k);
            try {
                fitness[k] = fitfun.value(arx.getColumn(k));
            } catch (TooManyEvaluationsException e) {
                break generationLoop;
            }
        }
        final int[] arindex = sortedIndices(fitness);
        final RealMatrix xold = xmean;
        final RealMatrix bestArx = selectColumns(arx, MathArrays.copyOf(arindex, mu));
        xmean = bestArx.multiply(weights);
        final RealMatrix bestArz = selectColumns(arz, MathArrays.copyOf(arindex, mu));
        final RealMatrix zmean = bestArz.multiply(weights);
        final boolean hsig = updateEvolutionPaths(zmean, xold);
        if (diagonalOnly <= 0) {
            updateCovariance(hsig, bestArx, arz, arindex, xold);
        } else {
            updateCovarianceDiagonalOnly(hsig, bestArz);
        }
        sigma *= Math.exp(Math.min(1, (normps/chiN - 1) * cs / damps));
        final double bestFitness = fitness[arindex[0]];
        final double worstFitness = fitness[arindex[arindex.length - 1]];
        if ((isMinimize && bestValue > bestFitness) ||
            (!isMinimize && bestValue < bestFitness)) {
            bestValue = bestFitness;
            lastResult = optimum;
            optimum = new PointValuePair(fitfun.repair(bestArx.getColumn(0)),
                                         isMinimize ? bestFitness : -bestFitness);
            if (getConvergenceChecker() != null &&
                lastResult != null) {
                if (getConvergenceChecker().converged(iterations, optimum, lastResult)) {
                    break generationLoop;
                }
            }
        }
        if (stopFitness != 0) {
            if (bestFitness < (isMinimize ? stopFitness : -stopFitness)) {
                break generationLoop;
            }
        }
        final double[] sqrtDiagC = sqrt(diagC).getColumn(0);
        final double[] pcCol = pc.getColumn(0);
        for (int i = 0; i < dimension; i++) {
            if (sigma * Math.max(Math.abs(pcCol[i]), sqrtDiagC[i]) > stopTolX) {
                break;
            }
            if (i >= dimension - 1) {
                break generationLoop;
            }
        }
        for (int i = 0; i < dimension; i++) {
            if (sigma * sqrtDiagC[i] > stopTolUpX) {
                break generationLoop;
            }
        }
        final double historyBest = min(fitnessHistory);
        final double historyWorst = max(fitnessHistory);
        if (iterations > 2 &&
            Math.max(historyWorst, worstFitness) -
            Math.min(historyBest, bestFitness) < stopTolFun) {
            break generationLoop;
        }
        if (iterations > fitnessHistory.length &&
            historyWorst - historyBest < stopTolHistFun) {
            break generationLoop;
        }
        if (max(diagD) / min(diagD) > 1e7) {
            break generationLoop;
        }
        if (getConvergenceChecker() != null) {
            final PointValuePair current
                = new PointValuePair(bestArx.getColumn(0),
                                     isMinimize ? bestFitness : -bestFitness);
            if (lastResult != null &&
                getConvergenceChecker().converged(iterations, current, lastResult)) {
                break generationLoop;
                }
            lastResult = current;
        }
        if (bestValue == fitness[arindex[(int)(0.1+lambda/4.)]]) {
            sigma = sigma * Math.exp(0.2 + cs / damps);
        }
        if (iterations > 2 && Math.max(historyWorst, bestFitness) -
            Math.min(historyBest, bestFitness) == 0) {
            sigma = sigma * Math.exp(0.2 + cs / damps);
        }
        push(fitnessHistory,bestFitness);
        fitfun.setValueRange(worstFitness-bestFitness);
        if (generateStatistics) {
            statisticsSigmaHistory.add(sigma);
            statisticsFitnessHistory.add(bestFitness);
            statisticsMeanHistory.add(xmean.transpose());
            statisticsDHistory.add(diagD.transpose().scalarMultiply(1E5));
        }
    }
    return optimum;
}