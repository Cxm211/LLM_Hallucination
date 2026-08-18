// buggy code
    protected BaseOptimizer(ConvergenceChecker<PAIR> checker) {
        this.checker = checker;

        evaluations = new Incrementor(0, new MaxEvalCallback());
        iterations = new Incrementor(0, new MaxIterCallback());
    }

    protected PointValuePair doOptimize() {
        final ConvergenceChecker<PointValuePair> checker = getConvergenceChecker();
        final double[] point = getStartPoint();
        final GoalType goal = getGoalType();
        final int n = point.length;
        double[] r = computeObjectiveGradient(point);
        if (goal == GoalType.MINIMIZE) {
            for (int i = 0; i < n; i++) {
                r[i] = -r[i];
            }
        }

        // Initial search direction.
        double[] steepestDescent = preconditioner.precondition(point, r);
        double[] searchDirection = steepestDescent.clone();

        double delta = 0;
        for (int i = 0; i < n; ++i) {
            delta += r[i] * searchDirection[i];
        }

        PointValuePair current = null;
        int iter = 0;
        int maxEval = getMaxEvaluations();
        while (true) {
            ++iter;

            final double objective = computeObjectiveValue(point);
            PointValuePair previous = current;
            current = new PointValuePair(point, objective);
            if (previous != null) {
                if (checker.converged(iter, previous, current)) {
                    // We have found an optimum.
                    return current;
                }
            }

            // Find the optimal step in the search direction.
            final UnivariateFunction lsf = new LineSearchFunction(point, searchDirection);
            final double uB = findUpperBound(lsf, 0, initialStep);
            // XXX Last parameters is set to a value close to zero in order to
            // work around the divergence problem in the "testCircleFitting"
            // unit test (see MATH-439).
            final double step = solver.solve(maxEval, lsf, 0, uB, 1e-15);
            maxEval -= solver.getEvaluations(); // Subtract used up evaluations.

            // Validate new point.
            for (int i = 0; i < point.length; ++i) {
                point[i] += step * searchDirection[i];
            }

            r = computeObjectiveGradient(point);
            if (goal == GoalType.MINIMIZE) {
                for (int i = 0; i < n; ++i) {
                    r[i] = -r[i];
                }
            }

            // Compute beta.
            final double deltaOld = delta;
            final double[] newSteepestDescent = preconditioner.precondition(point, r);
            delta = 0;
            for (int i = 0; i < n; ++i) {
                delta += r[i] * newSteepestDescent[i];
            }

            final double beta;
            switch (updateFormula) {
            case FLETCHER_REEVES:
                beta = delta / deltaOld;
                break;
            case POLAK_RIBIERE:
                double deltaMid = 0;
                for (int i = 0; i < r.length; ++i) {
                    deltaMid += r[i] * steepestDescent[i];
                }
                beta = (delta - deltaMid) / deltaOld;
                break;
            default:
                // Should never happen.
                throw new MathInternalError();
            }
            steepestDescent = newSteepestDescent;

            // Compute conjugate search direction.
            if (iter % n == 0 ||
                beta < 0) {
                // Break conjugation: reset search direction.
                searchDirection = steepestDescent.clone();
            } else {
                // Compute new conjugate search direction.
                for (int i = 0; i < n; ++i) {
                    searchDirection[i] = steepestDescent[i] + beta * searchDirection[i];
                }
            }
        }
    }

    protected PointValuePair doOptimize() {
         // -------------------- Initialization --------------------------------
        isMinimize = getGoalType().equals(GoalType.MINIMIZE);
        final FitnessFunction fitfun = new FitnessFunction();
        final double[] guess = getStartPoint();
        // number of objective variables/problem dimension
        dimension = guess.length;
        initializeCMA(guess);
        iterations = 0;
        double bestValue = fitfun.value(guess);
        push(fitnessHistory, bestValue);
        PointValuePair optimum
            = new PointValuePair(getStartPoint(),
                                 isMinimize ? bestValue : -bestValue);
        PointValuePair lastResult = null;

        // -------------------- Generation Loop --------------------------------

        generationLoop:
        for (iterations = 1; iterations <= maxIterations; iterations++) {

            // Generate and evaluate lambda offspring
            final RealMatrix arz = randn1(dimension, lambda);
            final RealMatrix arx = zeros(dimension, lambda);
            final double[] fitness = new double[lambda];
            // generate random offspring
            for (int k = 0; k < lambda; k++) {
                RealMatrix arxk = null;
                for (int i = 0; i < checkFeasableCount + 1; i++) {
                    if (diagonalOnly <= 0) {
                        arxk = xmean.add(BD.multiply(arz.getColumnMatrix(k))
                                         .scalarMultiply(sigma)); // m + sig * Normal(0,C)
                    } else {
                        arxk = xmean.add(times(diagD,arz.getColumnMatrix(k))
                                         .scalarMultiply(sigma));
                    }
                    if (i >= checkFeasableCount ||
                        fitfun.isFeasible(arxk.getColumn(0))) {
                        break;
                    }
                    // regenerate random arguments for row
                    arz.setColumn(k, randn(dimension));
                }
                copyColumn(arxk, 0, arx, k);
                try {
                    fitness[k] = fitfun.value(arx.getColumn(k)); // compute fitness
                } catch (TooManyEvaluationsException e) {
                    break generationLoop;
                }
            }
            // Sort by fitness and compute weighted mean into xmean
            final int[] arindex = sortedIndices(fitness);
            // Calculate new xmean, this is selection and recombination
            final RealMatrix xold = xmean; // for speed up of Eq. (2) and (3)
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
            // Adapt step size sigma - Eq. (5)
            sigma *= Math.exp(Math.min(1, (normps/chiN - 1) * cs / damps));
            final double bestFitness = fitness[arindex[0]];
            final double worstFitness = fitness[arindex[arindex.length - 1]];
            if (bestValue > bestFitness) {
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
            // handle termination criteria
            // Break, if fitness is good enough
            if (stopFitness != 0) { // only if stopFitness is defined
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
            // condition number of the covariance matrix exceeds 1e14
            if (max(diagD) / min(diagD) > 1e7) {
                break generationLoop;
            }
            // user defined termination
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
            // Adjust step size in case of equal function values (flat fitness)
            if (bestValue == fitness[arindex[(int)(0.1+lambda/4.)]]) {
                sigma = sigma * Math.exp(0.2 + cs / damps);
            }
            if (iterations > 2 && Math.max(historyWorst, bestFitness) -
                Math.min(historyBest, bestFitness) == 0) {
                sigma = sigma * Math.exp(0.2 + cs / damps);
            }
            // store best in history
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

    protected PointValuePair doOptimize() {
        checkParameters();

        final GoalType goal = getGoalType();
        final double[] guess = getStartPoint();
        final int n = guess.length;

        final double[][] direc = new double[n][n];
        for (int i = 0; i < n; i++) {
            direc[i][i] = 1;
        }

        final ConvergenceChecker<PointValuePair> checker
            = getConvergenceChecker();

        double[] x = guess;
        double fVal = computeObjectiveValue(x);
        double[] x1 = x.clone();
        int iter = 0;
        while (true) {
            ++iter;

            double fX = fVal;
            double fX2 = 0;
            double delta = 0;
            int bigInd = 0;
            double alphaMin = 0;

            for (int i = 0; i < n; i++) {
                final double[] d = MathArrays.copyOf(direc[i]);

                fX2 = fVal;

                final UnivariatePointValuePair optimum = line.search(x, d);
                fVal = optimum.getValue();
                alphaMin = optimum.getPoint();
                final double[][] result = newPointAndDirection(x, d, alphaMin);
                x = result[0];

                if ((fX2 - fVal) > delta) {
                    delta = fX2 - fVal;
                    bigInd = i;
                }
            }

            // Default convergence check.
            boolean stop = 2 * (fX - fVal) <=
                (relativeThreshold * (FastMath.abs(fX) + FastMath.abs(fVal)) +
                 absoluteThreshold);

            final PointValuePair previous = new PointValuePair(x1, fX);
            final PointValuePair current = new PointValuePair(x, fVal);
            if (!stop) { // User-defined stopping criteria.
                if (checker != null) {
                    stop = checker.converged(iter, previous, current);
                }
            }
            if (stop) {
                if (goal == GoalType.MINIMIZE) {
                    return (fVal < fX) ? current : previous;
                } else {
                    return (fVal > fX) ? current : previous;
                }
            }

            final double[] d = new double[n];
            final double[] x2 = new double[n];
            for (int i = 0; i < n; i++) {
                d[i] = x[i] - x1[i];
                x2[i] = 2 * x[i] - x1[i];
            }

            x1 = x.clone();
            fX2 = computeObjectiveValue(x2);

            if (fX > fX2) {
                double t = 2 * (fX + fX2 - 2 * fVal);
                double temp = fX - fVal - delta;
                t *= temp * temp;
                temp = fX - fX2;
                t -= delta * temp * temp;

                if (t < 0.0) {
                    final UnivariatePointValuePair optimum = line.search(x, d);
                    fVal = optimum.getValue();
                    alphaMin = optimum.getPoint();
                    final double[][] result = newPointAndDirection(x, d, alphaMin);
                    x = result[0];

                    final int lastInd = n - 1;
                    direc[bigInd] = direc[lastInd];
                    direc[lastInd] = result[1];
                }
            }
        }
    }

    protected PointValuePair doOptimize() {
        checkParameters();

        // Indirect call to "computeObjectiveValue" in order to update the
        // evaluations counter.
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

        // Initialize search.
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
                    // We have found an optimum.
                    return simplex.getPoint(0);
                }
            }

            // We still need to search.
            previous = simplex.getPoints();
            simplex.iterate(evalFunc, comparator);

			++iteration;
        }
    }

    public PointVectorValuePair doOptimize() {
        checkParameters();

        final ConvergenceChecker<PointVectorValuePair> checker
            = getConvergenceChecker();

        // Computation will be useless without a checker (see "for-loop").
        if (checker == null) {
            throw new NullArgumentException();
        }

        final double[] targetValues = getTarget();
        final int nR = targetValues.length; // Number of observed data.

        final RealMatrix weightMatrix = getWeight();
        // Diagonal of the weight matrix.
        final double[] residualsWeights = new double[nR];
        for (int i = 0; i < nR; i++) {
            residualsWeights[i] = weightMatrix.getEntry(i, i);
        }

        final double[] currentPoint = getStartPoint();
        final int nC = currentPoint.length;

        // iterate until convergence is reached
        PointVectorValuePair current = null;
        int iter = 0;
        for (boolean converged = false; !converged;) {
            ++iter;

            // evaluate the objective function and its jacobian
            PointVectorValuePair previous = current;
            // Value of the objective function at "currentPoint".
            final double[] currentObjective = computeObjectiveValue(currentPoint);
            final double[] currentResiduals = computeResiduals(currentObjective);
            final RealMatrix weightedJacobian = computeWeightedJacobian(currentPoint);
            current = new PointVectorValuePair(currentPoint, currentObjective);

            // build the linear problem
            final double[]   b = new double[nC];
            final double[][] a = new double[nC][nC];
            for (int i = 0; i < nR; ++i) {

                final double[] grad   = weightedJacobian.getRow(i);
                final double weight   = residualsWeights[i];
                final double residual = currentResiduals[i];

                // compute the normal equation
                final double wr = weight * residual;
                for (int j = 0; j < nC; ++j) {
                    b[j] += wr * grad[j];
                }

                // build the contribution matrix for measurement i
                for (int k = 0; k < nC; ++k) {
                    double[] ak = a[k];
                    double wgk = weight * grad[k];
                    for (int l = 0; l < nC; ++l) {
                        ak[l] += wgk * grad[l];
                    }
                }
            }

            try {
                // solve the linearized least squares problem
                RealMatrix mA = new BlockRealMatrix(a);
                DecompositionSolver solver = useLU ?
                        new LUDecomposition(mA).getSolver() :
                        new QRDecomposition(mA).getSolver();
                final double[] dX = solver.solve(new ArrayRealVector(b, false)).toArray();
                // update the estimated parameters
                for (int i = 0; i < nC; ++i) {
                    currentPoint[i] += dX[i];
                }
            } catch (SingularMatrixException e) {
                throw new ConvergenceException(LocalizedFormats.UNABLE_TO_SOLVE_SINGULAR_PROBLEM);
            }

            // Check convergence.
            if (previous != null) {
                converged = checker.converged(iter, previous, current);
                if (converged) {
                    setCost(computeCost(currentResiduals));
                    return current;
                }
            }
        }
        // Must never happen.
        throw new MathInternalError();
    }

    protected PointVectorValuePair doOptimize() {
        checkParameters();

        final int nR = getTarget().length; // Number of observed data.
        final double[] currentPoint = getStartPoint();
        final int nC = currentPoint.length; // Number of parameters.

        // arrays shared with the other private methods
        solvedCols  = FastMath.min(nR, nC);
        diagR       = new double[nC];
        jacNorm     = new double[nC];
        beta        = new double[nC];
        permutation = new int[nC];
        lmDir       = new double[nC];

        // local point
        double   delta   = 0;
        double   xNorm   = 0;
        double[] diag    = new double[nC];
        double[] oldX    = new double[nC];
        double[] oldRes  = new double[nR];
        double[] oldObj  = new double[nR];
        double[] qtf     = new double[nR];
        double[] work1   = new double[nC];
        double[] work2   = new double[nC];
        double[] work3   = new double[nC];

        final RealMatrix weightMatrixSqrt = getWeightSquareRoot();

        // Evaluate the function at the starting point and calculate its norm.
        double[] currentObjective = computeObjectiveValue(currentPoint);
        double[] currentResiduals = computeResiduals(currentObjective);
        PointVectorValuePair current = new PointVectorValuePair(currentPoint, currentObjective);
        double currentCost = computeCost(currentResiduals);

        // Outer loop.
        lmPar = 0;
        boolean firstIteration = true;
        int iter = 0;
        final ConvergenceChecker<PointVectorValuePair> checker = getConvergenceChecker();
        while (true) {
            ++iter;
            final PointVectorValuePair previous = current;

            // QR decomposition of the jacobian matrix
            qrDecomposition(computeWeightedJacobian(currentPoint));

            weightedResidual = weightMatrixSqrt.operate(currentResiduals);
            for (int i = 0; i < nR; i++) {
                qtf[i] = weightedResidual[i];
            }

            // compute Qt.res
            qTy(qtf);

            // now we don't need Q anymore,
            // so let jacobian contain the R matrix with its diagonal elements
            for (int k = 0; k < solvedCols; ++k) {
                int pk = permutation[k];
                weightedJacobian[k][pk] = diagR[pk];
            }

            if (firstIteration) {
                // scale the point according to the norms of the columns
                // of the initial jacobian
                xNorm = 0;
                for (int k = 0; k < nC; ++k) {
                    double dk = jacNorm[k];
                    if (dk == 0) {
                        dk = 1.0;
                    }
                    double xk = dk * currentPoint[k];
                    xNorm  += xk * xk;
                    diag[k] = dk;
                }
                xNorm = FastMath.sqrt(xNorm);

                // initialize the step bound delta
                delta = (xNorm == 0) ? initialStepBoundFactor : (initialStepBoundFactor * xNorm);
            }

            // check orthogonality between function vector and jacobian columns
            double maxCosine = 0;
            if (currentCost != 0) {
                for (int j = 0; j < solvedCols; ++j) {
                    int    pj = permutation[j];
                    double s  = jacNorm[pj];
                    if (s != 0) {
                        double sum = 0;
                        for (int i = 0; i <= j; ++i) {
                            sum += weightedJacobian[i][pj] * qtf[i];
                        }
                        maxCosine = FastMath.max(maxCosine, FastMath.abs(sum) / (s * currentCost));
                    }
                }
            }
            if (maxCosine <= orthoTolerance) {
                // Convergence has been reached.
                setCost(currentCost);
                return current;
            }

            // rescale if necessary
            for (int j = 0; j < nC; ++j) {
                diag[j] = FastMath.max(diag[j], jacNorm[j]);
            }

            // Inner loop.
            for (double ratio = 0; ratio < 1.0e-4;) {

                // save the state
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    oldX[pj] = currentPoint[pj];
                }
                final double previousCost = currentCost;
                double[] tmpVec = weightedResidual;
                weightedResidual = oldRes;
                oldRes    = tmpVec;
                tmpVec    = currentObjective;
                currentObjective = oldObj;
                oldObj    = tmpVec;

                // determine the Levenberg-Marquardt parameter
                determineLMParameter(qtf, delta, diag, work1, work2, work3);

                // compute the new point and the norm of the evolution direction
                double lmNorm = 0;
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    lmDir[pj] = -lmDir[pj];
                    currentPoint[pj] = oldX[pj] + lmDir[pj];
                    double s = diag[pj] * lmDir[pj];
                    lmNorm  += s * s;
                }
                lmNorm = FastMath.sqrt(lmNorm);
                // on the first iteration, adjust the initial step bound.
                if (firstIteration) {
                    delta = FastMath.min(delta, lmNorm);
                }

                // Evaluate the function at x + p and calculate its norm.
                currentObjective = computeObjectiveValue(currentPoint);
                currentResiduals = computeResiduals(currentObjective);
                current = new PointVectorValuePair(currentPoint, currentObjective);
                currentCost = computeCost(currentResiduals);

                // compute the scaled actual reduction
                double actRed = -1.0;
                if (0.1 * currentCost < previousCost) {
                    double r = currentCost / previousCost;
                    actRed = 1.0 - r * r;
                }

                // compute the scaled predicted reduction
                // and the scaled directional derivative
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    double dirJ = lmDir[pj];
                    work1[j] = 0;
                    for (int i = 0; i <= j; ++i) {
                        work1[i] += weightedJacobian[i][pj] * dirJ;
                    }
                }
                double coeff1 = 0;
                for (int j = 0; j < solvedCols; ++j) {
                    coeff1 += work1[j] * work1[j];
                }
                double pc2 = previousCost * previousCost;
                coeff1 = coeff1 / pc2;
                double coeff2 = lmPar * lmNorm * lmNorm / pc2;
                double preRed = coeff1 + 2 * coeff2;
                double dirDer = -(coeff1 + coeff2);

                // ratio of the actual to the predicted reduction
                ratio = (preRed == 0) ? 0 : (actRed / preRed);

                // update the step bound
                if (ratio <= 0.25) {
                    double tmp =
                        (actRed < 0) ? (0.5 * dirDer / (dirDer + 0.5 * actRed)) : 0.5;
                        if ((0.1 * currentCost >= previousCost) || (tmp < 0.1)) {
                            tmp = 0.1;
                        }
                        delta = tmp * FastMath.min(delta, 10.0 * lmNorm);
                        lmPar /= tmp;
                } else if ((lmPar == 0) || (ratio >= 0.75)) {
                    delta = 2 * lmNorm;
                    lmPar *= 0.5;
                }

                // test for successful iteration.
                if (ratio >= 1.0e-4) {
                    // successful iteration, update the norm
                    firstIteration = false;
                    xNorm = 0;
                    for (int k = 0; k < nC; ++k) {
                        double xK = diag[k] * currentPoint[k];
                        xNorm += xK * xK;
                    }
                    xNorm = FastMath.sqrt(xNorm);

                    // tests for convergence.
                    if (checker != null) {
                        // we use the vectorial convergence checker
                        if (checker.converged(iter, previous, current)) {
                            setCost(currentCost);
                            return current;
                        }
                    }
                } else {
                    // failed iteration, reset the previous values
                    currentCost = previousCost;
                    for (int j = 0; j < solvedCols; ++j) {
                        int pj = permutation[j];
                        currentPoint[pj] = oldX[pj];
                    }
                    tmpVec    = weightedResidual;
                    weightedResidual = oldRes;
                    oldRes    = tmpVec;
                    tmpVec    = currentObjective;
                    currentObjective = oldObj;
                    oldObj    = tmpVec;
                    // Reset "current" to previous values.
                    current = new PointVectorValuePair(currentPoint, currentObjective);
                }

                // Default convergence criteria.
                if ((FastMath.abs(actRed) <= costRelativeTolerance &&
                     preRed <= costRelativeTolerance &&
                     ratio <= 2.0) ||
                    delta <= parRelativeTolerance * xNorm) {
                    setCost(currentCost);
                    return current;
                }

                // tests for termination and stringent tolerances
                // (2.2204e-16 is the machine epsilon for IEEE754)
                if ((FastMath.abs(actRed) <= 2.2204e-16) && (preRed <= 2.2204e-16) && (ratio <= 2.0)) {
                    throw new ConvergenceException(LocalizedFormats.TOO_SMALL_COST_RELATIVE_TOLERANCE,
                                                   costRelativeTolerance);
                } else if (delta <= 2.2204e-16 * xNorm) {
                    throw new ConvergenceException(LocalizedFormats.TOO_SMALL_PARAMETERS_RELATIVE_TOLERANCE,
                                                   parRelativeTolerance);
                } else if (maxCosine <= 2.2204e-16)  {
                    throw new ConvergenceException(LocalizedFormats.TOO_SMALL_ORTHOGONALITY_TOLERANCE,
                                                   orthoTolerance);
                }
            }
        }
    }

// relevant test
// org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testPreconditions
    public void testPreconditions() {
        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2.5};
        double[][] zval = new double[xval.length][yval.length];

        BivariateGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(0);
        
        @SuppressWarnings("unused")
        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
        
        double[] wxval = new double[] {3, 2, 5, 6.5};
        try {
            p = interpolator.interpolate(wxval, yval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[] wyval = new double[] {-4, -3, -1, -1};
        try {
            p = interpolator.interpolate(xval, wyval, zval);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            
        }

        double[][] wzval = new double[xval.length][yval.length + 1];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wzval = new double[xval.length - 1][yval.length];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
        wzval = new double[xval.length][yval.length - 1];
        try {
            p = interpolator.interpolate(xval, yval, wzval);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException e) {
            
        }
    }

// org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testPlane
    public void testPlane() {
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x - 3 * y + 5
                        + ((int) (FastMath.abs(5 * x + 3 * y)) % 2 == 0 ? 1 : -1);
                }
            };

        BivariateGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(1);

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -1, 2, 2.5};
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
        double x, y;
        double expected, result;
        
        x = 4;
        y = -3;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("On sample point", expected, result, 2);

        x = 4.5;
        y = -1.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (middle of the patch)", expected, result, 2);

        x = 3.5;
        y = -3.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (border of the patch)", expected, result, 2);
    }

// org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest::testParaboloid
    public void testParaboloid() {
        BivariateFunction f = new BivariateFunction() {
                public double value(double x, double y) {
                    return 2 * x * x - 3 * y * y + 4 * x * y - 5
                        + ((int) (FastMath.abs(5 * x + 3 * y)) % 2 == 0 ? 1 : -1);
                }
            };

        BivariateGridInterpolator interpolator = new SmoothingPolynomialBicubicSplineInterpolator(4);

        double[] xval = new double[] {3, 4, 5, 6.5};
        double[] yval = new double[] {-4, -3, -2, -1, 0.5, 2.5};
        double[][] zval = new double[xval.length][yval.length];
        for (int i = 0; i < xval.length; i++) {
            for (int j = 0; j < yval.length; j++) {
                zval[i][j] = f.value(xval[i], yval[j]);
            }
        }

        BivariateFunction p = interpolator.interpolate(xval, yval, zval);
        double x, y;
        double expected, result;

        x = 5;
        y = 0.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("On sample point", expected, result, 2);

        x = 4.5;
        y = -1.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (middle of the patch)", expected, result, 2);

        x = 3.5;
        y = -3.5;
        expected = f.value(x, y);
        result = p.value(x, y);
        Assert.assertEquals("half-way between sample points (border of the patch)", expected, result, 2);
    }

// org.apache.commons.math3.fitting.CurveFitterTest::testMath303
    public void testMath303() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(optimizer);
        fitter.addObservedPoint(2.805d, 0.6934785852953367d);
        fitter.addObservedPoint(2.74333333333333d, 0.6306772025518496d);
        fitter.addObservedPoint(1.655d, 0.9474675497289684);
        fitter.addObservedPoint(1.725d, 0.9013594835804194d);

        ParametricUnivariateFunction sif = new SimpleInverseFunction();

        double[] initialguess1 = new double[1];
        initialguess1[0] = 1.0d;
        Assert.assertEquals(1, fitter.fit(sif, initialguess1).length);

        double[] initialguess2 = new double[2];
        initialguess2[0] = 1.0d;
        initialguess2[1] = .5d;
        Assert.assertEquals(2, fitter.fit(sif, initialguess2).length);
    }

// org.apache.commons.math3.fitting.CurveFitterTest::testMath304
    public void testMath304() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(optimizer);
        fitter.addObservedPoint(2.805d, 0.6934785852953367d);
        fitter.addObservedPoint(2.74333333333333d, 0.6306772025518496d);
        fitter.addObservedPoint(1.655d, 0.9474675497289684);
        fitter.addObservedPoint(1.725d, 0.9013594835804194d);

        ParametricUnivariateFunction sif = new SimpleInverseFunction();

        double[] initialguess1 = new double[1];
        initialguess1[0] = 1.0d;
        Assert.assertEquals(1.6357215104109237, fitter.fit(sif, initialguess1)[0], 1.0e-14);

        double[] initialguess2 = new double[1];
        initialguess2[0] = 10.0d;
        Assert.assertEquals(1.6357215104109237, fitter.fit(sif, initialguess1)[0], 1.0e-14);
    }

// org.apache.commons.math3.fitting.CurveFitterTest::testMath372
    public void testMath372() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        CurveFitter<ParametricUnivariateFunction> curveFitter = new CurveFitter<ParametricUnivariateFunction>(optimizer);

        curveFitter.addObservedPoint( 15,  4443);
        curveFitter.addObservedPoint( 31,  8493);
        curveFitter.addObservedPoint( 62, 17586);
        curveFitter.addObservedPoint(125, 30582);
        curveFitter.addObservedPoint(250, 45087);
        curveFitter.addObservedPoint(500, 50683);

        ParametricUnivariateFunction f = new ParametricUnivariateFunction() {
            public double value(double x, double ... parameters) {
                double a = parameters[0];
                double b = parameters[1];
                double c = parameters[2];
                double d = parameters[3];

                return d + ((a - d) / (1 + FastMath.pow(x / c, b)));
            }

            public double[] gradient(double x, double ... parameters) {
                double a = parameters[0];
                double b = parameters[1];
                double c = parameters[2];
                double d = parameters[3];

                double[] gradients = new double[4];
                double den = 1 + FastMath.pow(x / c, b);

                
                gradients[0] = 1 / den;

                
                
                gradients[1] = -((a - d) * FastMath.pow(x / c, b) * FastMath.log(x / c)) / (den * den);

                
                gradients[2] = (b * FastMath.pow(x / c, b - 1) * (x / (c * c)) * (a - d)) / (den * den);

                
                gradients[3] = 1 - (1 / den);

                return gradients;

            }
        };

        double[] initialGuess = new double[] { 1500, 0.95, 65, 35000 };
        double[] estimatedParameters = curveFitter.fit(f, initialGuess);

        Assert.assertEquals( 2411.00, estimatedParameters[0], 500.00);
        Assert.assertEquals(    1.62, estimatedParameters[1],   0.04);
        Assert.assertEquals(  111.22, estimatedParameters[2],   0.30);
        Assert.assertEquals(55347.47, estimatedParameters[3], 300.00);
        Assert.assertTrue(optimizer.getRMS() < 600.0);
    }

// org.apache.commons.math3.fitting.GaussianFitterTest::testFit01
    public void testFit01() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET1, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(3496978.1837704973, parameters[0], 1e-4);
        Assert.assertEquals(4.054933085999146, parameters[1], 1e-4);
        Assert.assertEquals(0.015039355620304326, parameters[2], 1e-4);
    }

// org.apache.commons.math3.fitting.GaussianFitterTest::testFit02
    public void testFit02() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        fitter.fit();
    }

// org.apache.commons.math3.fitting.GaussianFitterTest::testFit03
    public void testFit03() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(new double[][] {
            {4.0254623,  531026.0},
            {4.02804905, 664002.0}},
            fitter);
        fitter.fit();
    }

// org.apache.commons.math3.fitting.GaussianFitterTest::testFit04
    public void testFit04() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET2, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(233003.2967252038, parameters[0], 1e-4);
        Assert.assertEquals(-10.654887521095983, parameters[1], 1e-4);
        Assert.assertEquals(4.335937353196641, parameters[2], 1e-4);
    }

// org.apache.commons.math3.fitting.GaussianFitterTest::testFit05
    public void testFit05() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET3, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(283863.81929180305, parameters[0], 1e-4);
        Assert.assertEquals(-13.29641995105174, parameters[1], 1e-4);
        Assert.assertEquals(1.7297330293549908, parameters[2], 1e-4);
    }

// org.apache.commons.math3.fitting.GaussianFitterTest::testFit06
    public void testFit06() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET4, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(285250.66754309234, parameters[0], 1e-4);
        Assert.assertEquals(-13.528375695228455, parameters[1], 1e-4);
        Assert.assertEquals(1.5204344894331614, parameters[2], 1e-4);
    }

// org.apache.commons.math3.fitting.GaussianFitterTest::testFit07
    public void testFit07() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        addDatasetToGaussianFitter(DATASET5, fitter);
        double[] parameters = fitter.fit();

        Assert.assertEquals(3514384.729342235, parameters[0], 1e-4);
        Assert.assertEquals(4.054970307455625, parameters[1], 1e-4);
        Assert.assertEquals(0.015029412832160017, parameters[2], 1e-4);
    }

// org.apache.commons.math3.fitting.GaussianFitterTest::testMath519
    public void testMath519() {
        
        

        final double[] data = { 
            1.1143831578403364E-29,
            4.95281403484594E-28,
            1.1171347211930288E-26,
            1.7044813962636277E-25,
            1.9784716574832164E-24,
            1.8630236407866774E-23,
            1.4820532905097742E-22,
            1.0241963854632831E-21,
            6.275077366673128E-21,
            3.461808994532493E-20,
            1.7407124684715706E-19,
            8.056687953553974E-19,
            3.460193945992071E-18,
            1.3883326374011525E-17,
            5.233894983671116E-17,
            1.8630791465263745E-16,
            6.288759227922111E-16,
            2.0204433920597856E-15,
            6.198768938576155E-15,
            1.821419346860626E-14,
            5.139176445538471E-14,
            1.3956427429045787E-13,
            3.655705706448139E-13,
            9.253753324779779E-13,
            2.267636001476696E-12,
            5.3880460095836855E-12,
            1.2431632654852931E-11
        };

        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        for (int i = 0; i < data.length; i++) {
            fitter.addObservedPoint(i, data[i]);
        }
        final double[] p = fitter.fit();

        Assert.assertEquals(53.1572792, p[1], 1e-7);
        Assert.assertEquals(5.75214622, p[2], 1e-8);
    }

// org.apache.commons.math3.fitting.GaussianFitterTest::testMath798
    public void testMath798() {
        final GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());

        
        
        
        

        fitter.addObservedPoint(0.23, 395.0);
        
        fitter.addObservedPoint(1.14, 376.0);
        
        fitter.addObservedPoint(2.05, 163.0);
        
        fitter.addObservedPoint(2.95, 49.0);
        
        fitter.addObservedPoint(3.86, 16.0);
        
        fitter.addObservedPoint(4.77, 1.0);

        final double[] p = fitter.fit();

        
        Assert.assertEquals(420.8397296167364, p[0], 1e-12);
        Assert.assertEquals(0.603770729862231, p[1], 1e-15);
        Assert.assertEquals(1.0786447936766612, p[2], 1e-14);
    }

// org.apache.commons.math3.fitting.HarmonicFitterTest::testPreconditions1
    public void testPreconditions1() {
        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());

        fitter.fit();
    }

// org.apache.commons.math3.fitting.HarmonicFitterTest::testNoError
    public void testNoError() {
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 1.3; x += 0.01) {
            fitter.addObservedPoint(1, x, f.value(x));
        }

        final double[] fitted = fitter.fit();
        Assert.assertEquals(a, fitted[0], 1.0e-13);
        Assert.assertEquals(w, fitted[1], 1.0e-13);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1e-13);

        HarmonicOscillator ff = new HarmonicOscillator(fitted[0], fitted[1], fitted[2]);

        for (double x = -1.0; x < 1.0; x += 0.01) {
            Assert.assertTrue(FastMath.abs(f.value(x) - ff.value(x)) < 1e-13);
        }
    }

// org.apache.commons.math3.fitting.HarmonicFitterTest::test1PercentError
    public void test1PercentError() {
        Random randomizer = new Random(64925784252l);
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1, x,
                                    f.value(x) + 0.01 * randomizer.nextGaussian());
        }

        final double[] fitted = fitter.fit();
        Assert.assertEquals(a, fitted[0], 7.6e-4);
        Assert.assertEquals(w, fitted[1], 2.7e-3);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1.3e-2);
    }

// org.apache.commons.math3.fitting.HarmonicFitterTest::testTinyVariationsData
    public void testTinyVariationsData() {
        Random randomizer = new Random(64925784252l);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1, x, 1e-7 * randomizer.nextGaussian());
        }

        fitter.fit();
        
        
    }

// org.apache.commons.math3.fitting.HarmonicFitterTest::testInitialGuess
    public void testInitialGuess() {
        Random randomizer = new Random(45314242l);
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());
        for (double x = 0.0; x < 10.0; x += 0.1) {
            fitter.addObservedPoint(1, x,
                                    f.value(x) + 0.01 * randomizer.nextGaussian());
        }

        final double[] fitted = fitter.fit(new double[] { 0.15, 3.6, 4.5 });
        Assert.assertEquals(a, fitted[0], 1.2e-3);
        Assert.assertEquals(w, fitted[1], 3.3e-3);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1.7e-2);
    }

// org.apache.commons.math3.fitting.HarmonicFitterTest::testUnsorted
    public void testUnsorted() {
        Random randomizer = new Random(64925784252l);
        final double a = 0.2;
        final double w = 3.4;
        final double p = 4.1;
        HarmonicOscillator f = new HarmonicOscillator(a, w, p);

        HarmonicFitter fitter =
            new HarmonicFitter(new LevenbergMarquardtOptimizer());

        
        int size = 100;
        double[] xTab = new double[size];
        double[] yTab = new double[size];
        for (int i = 0; i < size; ++i) {
            xTab[i] = 0.1 * i;
            yTab[i] = f.value(xTab[i]) + 0.01 * randomizer.nextGaussian();
        }

        
        for (int i = 0; i < size; ++i) {
            int i1 = randomizer.nextInt(size);
            int i2 = randomizer.nextInt(size);
            double xTmp = xTab[i1];
            double yTmp = yTab[i1];
            xTab[i1] = xTab[i2];
            yTab[i1] = yTab[i2];
            xTab[i2] = xTmp;
            yTab[i2] = yTmp;
        }

        
        for (int i = 0; i < size; ++i) {
            fitter.addObservedPoint(1, xTab[i], yTab[i]);
        }

        final double[] fitted = fitter.fit();
        Assert.assertEquals(a, fitted[0], 7.6e-4);
        Assert.assertEquals(w, fitted[1], 3.5e-3);
        Assert.assertEquals(p, MathUtils.normalizeAngle(fitted[2], p), 1.5e-2);
    }

// org.apache.commons.math3.fitting.HarmonicFitterTest::testMath844
    public void testMath844() {
        final double[] y = { 0, 1, 2, 3, 2, 1,
                             0, -1, -2, -3, -2, -1,
                             0, 1, 2, 3, 2, 1,
                             0, -1, -2, -3, -2, -1,
                             0, 1, 2, 3, 2, 1, 0 };
        final int len = y.length;
        final WeightedObservedPoint[] points = new WeightedObservedPoint[len];
        for (int i = 0; i < len; i++) {
            points[i] = new WeightedObservedPoint(1, i, y[i]);
        }

        
        
        
        
        
        final HarmonicFitter.ParameterGuesser guesser
            = new HarmonicFitter.ParameterGuesser(points);
    }

// org.apache.commons.math3.fitting.PolynomialFitterTest::testFit
    public void testFit() {
        final RealDistribution rng = new UniformRealDistribution(-100, 100);
        rng.reseedRandomGenerator(64925784252L);

        final LevenbergMarquardtOptimizer optim = new LevenbergMarquardtOptimizer();
        final PolynomialFitter fitter = new PolynomialFitter(optim);
        final double[] coeff = { 12.9, -3.4, 2.1 }; 
        final PolynomialFunction f = new PolynomialFunction(coeff);

        
        for (int i = 0; i < 100; i++) {
            final double x = rng.sample();
            fitter.addObservedPoint(x, f.value(x));
        }

        
        final double[] best = fitter.fit(new double[] { -1e-20, 3e15, -5e25 });

        TestUtils.assertEquals("best != coeff", coeff, best, 1e-12);
    }

// org.apache.commons.math3.fitting.PolynomialFitterTest::testNoError
    public void testNoError() {
        Random randomizer = new Random(64925784252l);
        for (int degree = 1; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
            for (int i = 0; i <= degree; ++i) {
                fitter.addObservedPoint(1.0, i, p.value(i));
            }

            final double[] init = new double[degree + 1];
            PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                               (1.0 + FastMath.abs(p.value(x)));
                Assert.assertEquals(0.0, error, 1.0e-6);
            }
        }
    }

// org.apache.commons.math3.fitting.PolynomialFitterTest::testSmallError
    public void testSmallError() {
        Random randomizer = new Random(53882150042l);
        double maxError = 0;
        for (int degree = 0; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
            for (double x = -1.0; x < 1.0; x += 0.01) {
                fitter.addObservedPoint(1.0, x,
                                        p.value(x) + 0.1 * randomizer.nextGaussian());
            }

            final double[] init = new double[degree + 1];
            PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                              (1.0 + FastMath.abs(p.value(x)));
                maxError = FastMath.max(maxError, error);
                Assert.assertTrue(FastMath.abs(error) < 0.1);
            }
        }
        Assert.assertTrue(maxError > 0.01);
    }

// org.apache.commons.math3.fitting.PolynomialFitterTest::testMath798
    public void testMath798() {
        final double tol = 1e-14;
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol);
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 3;

        final double[] lm = doMath798(new LevenbergMarquardtOptimizer(checker), maxEval, init);
        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);

        for (int i = 0; i <= 1; i++) {
            Assert.assertEquals(lm[i], gn[i], tol);
        }
    }

// org.apache.commons.math3.fitting.PolynomialFitterTest::testMath798WithToleranceTooLow
    public void testMath798WithToleranceTooLow() {
        final double tol = 1e-100;
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol);
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 10000; 

        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);
    }

// org.apache.commons.math3.fitting.PolynomialFitterTest::testMath798WithToleranceTooLowButNoException
    public void testMath798WithToleranceTooLowButNoException() {
        final double tol = 1e-100;
        final double[] init = new double[] { 0, 0 };
        final int maxEval = 10000; 
        final SimpleVectorValueChecker checker = new SimpleVectorValueChecker(tol, tol, maxEval);

        final double[] lm = doMath798(new LevenbergMarquardtOptimizer(checker), maxEval, init);
        final double[] gn = doMath798(new GaussNewtonOptimizer(checker), maxEval, init);

        for (int i = 0; i <= 1; i++) {
            Assert.assertEquals(lm[i], gn[i], 1e-15);
        }
    }

// org.apache.commons.math3.fitting.PolynomialFitterTest::testRedundantSolvable
    public void testRedundantSolvable() {
        
        checkUnsolvableProblem(new LevenbergMarquardtOptimizer(), true);
    }

// org.apache.commons.math3.fitting.PolynomialFitterTest::testRedundantUnsolvable
    public void testRedundantUnsolvable() {
        
        checkUnsolvableProblem(new GaussNewtonOptimizer(true, new SimpleVectorValueChecker(1e-15, 1e-15)), false);
    }

// org.apache.commons.math3.fitting.PolynomialFitterTest::testLargeSample
    public void testLargeSample() {
        Random randomizer = new Random(0x5551480dca5b369bl);
        double maxError = 0;
        for (int degree = 0; degree < 10; ++degree) {
            PolynomialFunction p = buildRandomPolynomial(degree, randomizer);

            PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
            for (int i = 0; i < 40000; ++i) {
                double x = -1.0 + i / 20000.0;
                fitter.addObservedPoint(1.0, x,
                                        p.value(x) + 0.1 * randomizer.nextGaussian());
            }

            final double[] init = new double[degree + 1];
            PolynomialFunction fitted = new PolynomialFunction(fitter.fit(init));

            for (double x = -1.0; x < 1.0; x += 0.01) {
                double error = FastMath.abs(p.value(x) - fitted.value(x)) /
                              (1.0 + FastMath.abs(p.value(x)));
                maxError = FastMath.max(maxError, error);
                Assert.assertTrue(FastMath.abs(error) < 0.01);
            }
        }
        Assert.assertTrue(maxError > 0.001);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath828
    public void testMath828() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
                new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 0.0);
        
        ArrayList <LinearConstraint>constraints = new ArrayList<LinearConstraint>();

        constraints.add(new LinearConstraint(new double[] {0.0, 39.0, 23.0, 96.0, 15.0, 48.0, 9.0, 21.0, 48.0, 36.0, 76.0, 19.0, 88.0, 17.0, 16.0, 36.0,}, Relationship.GEQ, 15.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 59.0, 93.0, 12.0, 29.0, 78.0, 73.0, 87.0, 32.0, 70.0, 68.0, 24.0, 11.0, 26.0, 65.0, 25.0,}, Relationship.GEQ, 29.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 74.0, 5.0, 82.0, 6.0, 97.0, 55.0, 44.0, 52.0, 54.0, 5.0, 93.0, 91.0, 8.0, 20.0, 97.0,}, Relationship.GEQ, 6.0));
        constraints.add(new LinearConstraint(new double[] {8.0, -3.0, -28.0, -72.0, -8.0, -31.0, -31.0, -74.0, -47.0, -59.0, -24.0, -57.0, -56.0, -16.0, -92.0, -59.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {25.0, -7.0, -99.0, -78.0, -25.0, -14.0, -16.0, -89.0, -39.0, -56.0, -53.0, -9.0, -18.0, -26.0, -11.0, -61.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {33.0, -95.0, -15.0, -4.0, -33.0, -3.0, -20.0, -96.0, -27.0, -13.0, -80.0, -24.0, -3.0, -13.0, -57.0, -76.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {7.0, -95.0, -39.0, -93.0, -7.0, -94.0, -94.0, -62.0, -76.0, -26.0, -53.0, -57.0, -31.0, -76.0, -53.0, -52.0,}, Relationship.GEQ, 0.0));
        
        double epsilon = 1e-6;
        PointValuePair solution = new SimplexSolver().optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                               GoalType.MINIMIZE, new NonNegativeConstraint(true));
        Assert.assertEquals(1.0d, solution.getValue(), epsilon);
        Assert.assertTrue(validSolution(solution, constraints, epsilon));
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath828Cycle
    public void testMath828Cycle() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(
                new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, 0.0);
        
        ArrayList <LinearConstraint>constraints = new ArrayList<LinearConstraint>();

        constraints.add(new LinearConstraint(new double[] {0.0, 16.0, 14.0, 69.0, 1.0, 85.0, 52.0, 43.0, 64.0, 97.0, 14.0, 74.0, 89.0, 28.0, 94.0, 58.0, 13.0, 22.0, 21.0, 17.0, 30.0, 25.0, 1.0, 59.0, 91.0, 78.0, 12.0, 74.0, 56.0, 3.0, 88.0,}, Relationship.GEQ, 91.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 60.0, 40.0, 81.0, 71.0, 72.0, 46.0, 45.0, 38.0, 48.0, 40.0, 17.0, 33.0, 85.0, 64.0, 32.0, 84.0, 3.0, 54.0, 44.0, 71.0, 67.0, 90.0, 95.0, 54.0, 99.0, 99.0, 29.0, 52.0, 98.0, 9.0,}, Relationship.GEQ, 54.0));
        constraints.add(new LinearConstraint(new double[] {0.0, 41.0, 12.0, 86.0, 90.0, 61.0, 31.0, 41.0, 23.0, 89.0, 17.0, 74.0, 44.0, 27.0, 16.0, 47.0, 80.0, 32.0, 11.0, 56.0, 68.0, 82.0, 11.0, 62.0, 62.0, 53.0, 39.0, 16.0, 48.0, 1.0, 63.0,}, Relationship.GEQ, 62.0));
        constraints.add(new LinearConstraint(new double[] {83.0, -76.0, -94.0, -19.0, -15.0, -70.0, -72.0, -57.0, -63.0, -65.0, -22.0, -94.0, -22.0, -88.0, -86.0, -89.0, -72.0, -16.0, -80.0, -49.0, -70.0, -93.0, -95.0, -17.0, -83.0, -97.0, -31.0, -47.0, -31.0, -13.0, -23.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {41.0, -96.0, -41.0, -48.0, -70.0, -43.0, -43.0, -43.0, -97.0, -37.0, -85.0, -70.0, -45.0, -67.0, -87.0, -69.0, -94.0, -54.0, -54.0, -92.0, -79.0, -10.0, -35.0, -20.0, -41.0, -41.0, -65.0, -25.0, -12.0, -8.0, -46.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {27.0, -42.0, -65.0, -49.0, -53.0, -42.0, -17.0, -2.0, -61.0, -31.0, -76.0, -47.0, -8.0, -93.0, -86.0, -62.0, -65.0, -63.0, -22.0, -43.0, -27.0, -23.0, -32.0, -74.0, -27.0, -63.0, -47.0, -78.0, -29.0, -95.0, -73.0,}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {15.0, -46.0, -41.0, -83.0, -98.0, -99.0, -21.0, -35.0, -7.0, -14.0, -80.0, -63.0, -18.0, -42.0, -5.0, -34.0, -56.0, -70.0, -16.0, -18.0, -74.0, -61.0, -47.0, -41.0, -15.0, -79.0, -18.0, -47.0, -88.0, -68.0, -55.0,}, Relationship.GEQ, 0.0));
        
        double epsilon = 1e-6;
        PointValuePair solution = new SimplexSolver().optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                               GoalType.MINIMIZE, new NonNegativeConstraint(true));
        Assert.assertEquals(1.0d, solution.getValue(), epsilon);
        Assert.assertTrue(validSolution(solution, constraints, epsilon));        
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath781
    public void testMath781() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 6, 7 }, 0);

        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2, 1 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { -1, 1, 1 }, Relationship.LEQ, -1));
        constraints.add(new LinearConstraint(new double[] { 2, -3, 1 }, Relationship.LEQ, -1));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MAXIMIZE, new NonNegativeConstraint(false));

        Assert.assertTrue(Precision.compareTo(solution.getPoint()[0], 0.0d, epsilon) > 0);
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[1], 0.0d, epsilon) > 0);
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[2], 0.0d, epsilon) < 0);
        Assert.assertEquals(2.0d, solution.getValue(), epsilon);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath713NegativeVariable
    public void testMath713NegativeVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 1.0}, 0.0d);
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.EQ, 1));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MINIMIZE, new NonNegativeConstraint(true));

        Assert.assertTrue(Precision.compareTo(solution.getPoint()[0], 0.0d, epsilon) >= 0);
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[1], 0.0d, epsilon) >= 0);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath434NegativeVariable
    public void testMath434NegativeVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {0.0, 0.0, 1.0}, 0.0d);
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 1, 0}, Relationship.EQ, 5));
        constraints.add(new LinearConstraint(new double[] {0, 0, 1}, Relationship.GEQ, -10));

        double epsilon = 1e-6;
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MINIMIZE, new NonNegativeConstraint(false));

        Assert.assertEquals(5.0, solution.getPoint()[0] + solution.getPoint()[1], epsilon);
        Assert.assertEquals(-10.0, solution.getPoint()[2], epsilon);
        Assert.assertEquals(-10.0, solution.getValue(), epsilon);

    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath434UnfeasibleSolution
    public void testMath434UnfeasibleSolution() {
        double epsilon = 1e-6;

        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 0.0}, 0.0);
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {epsilon/2, 0.5}, Relationship.EQ, 0));
        constraints.add(new LinearConstraint(new double[] {1e-3, 0.1}, Relationship.EQ, 10));

        SimplexSolver solver = new SimplexSolver();
        
        solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                        GoalType.MINIMIZE, new NonNegativeConstraint(true));
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath434PivotRowSelection
    public void testMath434PivotRowSelection() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0}, 0.0);

        double epsilon = 1e-6;
        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {200}, Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] {100}, Relationship.GEQ, 0.499900001));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MINIMIZE, new NonNegativeConstraint(false));
        
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[0] * 200.d, 1.d, epsilon) >= 0);
        Assert.assertEquals(0.0050, solution.getValue(), epsilon);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath434PivotRowSelection2
    public void testMath434PivotRowSelection2() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {0.0d, 1.0d, 1.0d, 0.0d, 0.0d, 0.0d, 0.0d}, 0.0d);

        ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1.0d, -0.1d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}, Relationship.EQ, -0.1d));
        constraints.add(new LinearConstraint(new double[] {1.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}, Relationship.GEQ, -1e-18d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 1.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d}, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 0.0d, 1.0d, 0.0d, -0.0128588d, 1e-5d}, Relationship.EQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 0.0d, 0.0d, 1.0d, 1e-5d, -0.0128586d}, Relationship.EQ, 1e-10d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 1.0d, -1.0d, 0.0d, 0.0d, 0.0d}, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 1.0d, 1.0d, 0.0d, 0.0d, 0.0d}, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 1.0d, 0.0d, -1.0d, 0.0d, 0.0d}, Relationship.GEQ, 0.0d));
        constraints.add(new LinearConstraint(new double[] {0.0d, 0.0d, 1.0d, 0.0d, 1.0d, 0.0d, 0.0d}, Relationship.GEQ, 0.0d));

        double epsilon = 1e-7;
        SimplexSolver simplex = new SimplexSolver();
        PointValuePair solution = simplex.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                   GoalType.MINIMIZE, new NonNegativeConstraint(false));
        
        Assert.assertTrue(Precision.compareTo(solution.getPoint()[0], -1e-18d, epsilon) >= 0);
        Assert.assertEquals(1.0d, solution.getPoint()[1], epsilon);        
        Assert.assertEquals(0.0d, solution.getPoint()[2], epsilon);
        Assert.assertEquals(1.0d, solution.getValue(), epsilon);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath272
    public void testMath272() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1, 0 }, Relationship.GEQ,  1));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1 }, Relationship.GEQ,  1));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 }, Relationship.GEQ,  1));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MINIMIZE, new NonNegativeConstraint(true));

        Assert.assertEquals(0.0, solution.getPoint()[0], .0000001);
        Assert.assertEquals(1.0, solution.getPoint()[1], .0000001);
        Assert.assertEquals(1.0, solution.getPoint()[2], .0000001);
        Assert.assertEquals(3.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath286
    public void testMath286() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.6, 0.4 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0, 0, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0, 0, 0 }, Relationship.GEQ, 8.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 1, 0 }, Relationship.GEQ, 5.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MAXIMIZE, new NonNegativeConstraint(true));

        Assert.assertEquals(25.8, solution.getValue(), .0000001);
        Assert.assertEquals(23.0, solution.getPoint()[0] + solution.getPoint()[2] + solution.getPoint()[4], 0.0000001);
        Assert.assertEquals(23.0, solution.getPoint()[1] + solution.getPoint()[3] + solution.getPoint()[5], 0.0000001);
        Assert.assertTrue(solution.getPoint()[0] >= 10.0 - 0.0000001);
        Assert.assertTrue(solution.getPoint()[2] >= 8.0 - 0.0000001);
        Assert.assertTrue(solution.getPoint()[4] >= 5.0 - 0.0000001);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testDegeneracy
    public void testDegeneracy() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.7 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 18.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 8.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MINIMIZE, new NonNegativeConstraint(true));
        Assert.assertEquals(13.6, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath288
    public void testMath288() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 7, 3, 0, 0 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 3, 0, -5, 0 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 2, 0, 0, -5 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 0, 3, 0, -5 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0 }, Relationship.LEQ, 1.0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MAXIMIZE, new NonNegativeConstraint(true));
        Assert.assertEquals(10.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath290GEQ
    public void testMath290GEQ() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.GEQ, -1.0));
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MINIMIZE, new NonNegativeConstraint(true));
        Assert.assertEquals(0, solution.getValue(), .0000001);
        Assert.assertEquals(0, solution.getPoint()[0], .0000001);
        Assert.assertEquals(0, solution.getPoint()[1], .0000001);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath290LEQ
    public void testMath290LEQ() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.LEQ, -1.0));
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                        GoalType.MINIMIZE, new NonNegativeConstraint(true));
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath293
    public void testMath293() {
      LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.4, 0.6}, 0 );
      Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0.8, 0.2, 0.0, 0.0, 0.0, 0.0 }, Relationship.GEQ, 10.0));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.7, 0.3, 0.0, 0.0 }, Relationship.GEQ, 10.0));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.4, 0.6 }, Relationship.GEQ, 10.0));

      SimplexSolver solver = new SimplexSolver();
      PointValuePair solution1 = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                 GoalType.MAXIMIZE, new NonNegativeConstraint(true));

      Assert.assertEquals(15.7143, solution1.getPoint()[0], .0001);
      Assert.assertEquals(0.0, solution1.getPoint()[1], .0001);
      Assert.assertEquals(14.2857, solution1.getPoint()[2], .0001);
      Assert.assertEquals(0.0, solution1.getPoint()[3], .0001);
      Assert.assertEquals(0.0, solution1.getPoint()[4], .0001);
      Assert.assertEquals(30.0, solution1.getPoint()[5], .0001);
      Assert.assertEquals(40.57143, solution1.getValue(), .0001);

      double valA = 0.8 * solution1.getPoint()[0] + 0.2 * solution1.getPoint()[1];
      double valB = 0.7 * solution1.getPoint()[2] + 0.3 * solution1.getPoint()[3];
      double valC = 0.4 * solution1.getPoint()[4] + 0.6 * solution1.getPoint()[5];

      f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.4, 0.6}, 0 );
      constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0.8, 0.2, 0.0, 0.0, 0.0, 0.0 }, Relationship.GEQ, valA));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.7, 0.3, 0.0, 0.0 }, Relationship.GEQ, valB));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.4, 0.6 }, Relationship.GEQ, valC));

      PointValuePair solution2 = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                 GoalType.MAXIMIZE, new NonNegativeConstraint(true));
      Assert.assertEquals(40.57143, solution2.getValue(), .0001);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMath930
    public void testMath930() {
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, -1, -1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, -1, -1, 1, 1, -1, -1, 1, -1, 1, 1, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 1, 0, -1, 0, -1, 0, 1, 0, -1, 0, 1, 0, 1, 0, -1, 0, -1, 0, 1, 0, 1, 0, -1, 0, 1, 0, -1, 0, -1, 0, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.628803}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.676993}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 1, -1, 0, 0, -1, 1, 0, 0, -1, 1, 0, 0, 1, -1, 0, 0, -1, 1, 0, 0, 1, -1, 0, 0, 1, -1, 0, 0, -1, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.136677}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.444434}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 1, 0, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, 1, 0, 0, 0, -1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.254028}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.302218}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 1, -1, -1, 1, 0, 0, 0, 0, -1, 1, 1, -1, 0, 0, 0, 0, -1, 1, 1, -1, 0, 0, 0, 0, 1, -1, -1, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.653981}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.690437}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 1, 0, -1, 0, 0, 0, 0, 0, -1, 0, 1, 0, 0, 0, 0, 0, -1, 0, 1, 0, 0, 0, 0, 0, 1, 0, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.423786}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.486717}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, -1, 1, 0, 0, 0, 0, 0, 0, -1, 1, 0, 0, 0, 0, 0, 0, 1, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.049232}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.304747}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.129826}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.205625}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -1, 1, -1, 1, 1, -1, 0, 0, 0, 0, 0, 0, 0, 0, -1, 1, 1, -1, 1, -1, -1, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.621944}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.764385}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, -1, 0, -1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 1, 0, 1, 0, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.432572}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.480762}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, -1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 1, 0, 0, 1, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.055983}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.11378}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.009607}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.057797}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 1, 1, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.407308}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.452749}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.269677}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.321806}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.049232}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.06902}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.028754}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.484254}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.524607}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, -1, 0, -1, 0, 1, 0, -1, 0, 1, 0, 1, 0, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.385492}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.430134}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, -1, 1, 0, 0, -1, 1, 0, 0, 1, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.34983}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.375781}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.254028}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.281308}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -1, 1, 0, 0, 0, 0, -1, 1, 1, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.304995}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.345347}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, -1, 0, 0, 0, 0, 0, -1, 0, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.288899}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.332212}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, -1, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.14351}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.17057}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, -0.129826}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, -0.157435}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -1, 1, -1, 1, 1, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -1}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, -1, 0, -1, 0, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, -0.141071}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, -0.232574}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, -1, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, -0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, -1}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, -0.009607}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, -0.057797}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, -1, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, -1}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, -0.091644}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, -0.203531}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, -0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, -1}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0}, Relationship.GEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -0.028754}, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.EQ, 1.0));
        
        double[] objFunctionCoeff = new double[33];
        objFunctionCoeff[3] = 1;
        LinearObjectiveFunction f = new LinearObjectiveFunction(objFunctionCoeff, 0);
        SimplexSolver solver = new SimplexSolver(1e-4, 10, 1e-6);
        
        PointValuePair solution = solver.optimize(new MaxIter(1000), f, new LinearConstraintSet(constraints),
                                                  GoalType.MINIMIZE, new NonNegativeConstraint(true));
        Assert.assertEquals(0.3752298, solution.getValue(), 1e-4);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testSimplexSolver
    public void testSimplexSolver() {
        LinearObjectiveFunction f =
            new LinearObjectiveFunction(new double[] { 15, 10 }, 7);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 4));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MAXIMIZE, new NonNegativeConstraint(true));
        Assert.assertEquals(2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(2.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(57.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testSingleVariableAndConstraint
    public void testSingleVariableAndConstraint() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 10));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MAXIMIZE, new NonNegativeConstraint(false));
        Assert.assertEquals(10.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(30.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testModelWithNoArtificialVars
    public void testModelWithNoArtificialVars() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MAXIMIZE, new NonNegativeConstraint(false));
        Assert.assertEquals(2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(2.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(50.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testMinimization
    public void testMinimization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, -5);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MINIMIZE, new NonNegativeConstraint(false));
        Assert.assertEquals(4.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(0.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(-13.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testSolutionWithNegativeDecisionVariable
    public void testSolutionWithNegativeDecisionVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 14));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MAXIMIZE, new NonNegativeConstraint(false));
        Assert.assertEquals(-2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(8.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(12.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testInfeasibleSolution
    public void testInfeasibleSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.GEQ, 3));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                        GoalType.MAXIMIZE, new NonNegativeConstraint(false));
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testUnboundedSolution
    public void testUnboundedSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 2));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                        GoalType.MAXIMIZE, new NonNegativeConstraint(false));
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testRestrictVariablesToNonNegative
    public void testRestrictVariablesToNonNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 409, 523, 70, 204, 339 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {    43,   56, 345,  56,    5 }, Relationship.LEQ,  4567456));
        constraints.add(new LinearConstraint(new double[] {    12,   45,   7,  56,   23 }, Relationship.LEQ,    56454));
        constraints.add(new LinearConstraint(new double[] {     8,  768,   0,  34, 7456 }, Relationship.LEQ,  1923421));
        constraints.add(new LinearConstraint(new double[] { 12342, 2342,  34, 678, 2342 }, Relationship.GEQ,     4356));
        constraints.add(new LinearConstraint(new double[] {    45,  678,  76,  52,   23 }, Relationship.EQ,    456356));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MAXIMIZE, new NonNegativeConstraint(true));
        Assert.assertEquals(2902.92783505155, solution.getPoint()[0], .0000001);
        Assert.assertEquals(480.419243986254, solution.getPoint()[1], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[2], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[3], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[4], .0000001);
        Assert.assertEquals(1438556.7491409, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testEpsilon
    public void testEpsilon() {
      LinearObjectiveFunction f =
          new LinearObjectiveFunction(new double[] { 10, 5, 1 }, 0);
      Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] {  9, 8, 0 }, Relationship.EQ,  17));
      constraints.add(new LinearConstraint(new double[] {  0, 7, 8 }, Relationship.LEQ,  7));
      constraints.add(new LinearConstraint(new double[] { 10, 0, 2 }, Relationship.LEQ, 10));

      SimplexSolver solver = new SimplexSolver();
      PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                GoalType.MAXIMIZE, new NonNegativeConstraint(false));
      Assert.assertEquals(1.0, solution.getPoint()[0], 0.0);
      Assert.assertEquals(1.0, solution.getPoint()[1], 0.0);
      Assert.assertEquals(0.0, solution.getPoint()[2], 0.0);
      Assert.assertEquals(15.0, solution.getValue(), 0.0);
  }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testTrivialModel
    public void testTrivialModel() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ,  0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MAXIMIZE, new NonNegativeConstraint(true));
        Assert.assertEquals(0, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optim.linear.SimplexSolverTest::testLargeModel
    public void testLargeModel() {
        double[] objective = new double[] {
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 12, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           12, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 12, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 12, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 12, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 12, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1};

        LinearObjectiveFunction f = new LinearObjectiveFunction(objective, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(equationFromString(objective.length, "x0 + x1 + x2 + x3 - x12 = 0"));
        constraints.add(equationFromString(objective.length, "x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 - x13 = 0"));
        constraints.add(equationFromString(objective.length, "x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 >= 49"));
        constraints.add(equationFromString(objective.length, "x0 + x1 + x2 + x3 >= 42"));
        constraints.add(equationFromString(objective.length, "x14 + x15 + x16 + x17 - x26 = 0"));
        constraints.add(equationFromString(objective.length, "x18 + x19 + x20 + x21 + x22 + x23 + x24 + x25 - x27 = 0"));
        constraints.add(equationFromString(objective.length, "x14 + x15 + x16 + x17 - x12 = 0"));
        constraints.add(equationFromString(objective.length, "x18 + x19 + x20 + x21 + x22 + x23 + x24 + x25 - x13 = 0"));
        constraints.add(equationFromString(objective.length, "x28 + x29 + x30 + x31 - x40 = 0"));
        constraints.add(equationFromString(objective.length, "x32 + x33 + x34 + x35 + x36 + x37 + x38 + x39 - x41 = 0"));
        constraints.add(equationFromString(objective.length, "x32 + x33 + x34 + x35 + x36 + x37 + x38 + x39 >= 49"));
        constraints.add(equationFromString(objective.length, "x28 + x29 + x30 + x31 >= 42"));
        constraints.add(equationFromString(objective.length, "x42 + x43 + x44 + x45 - x54 = 0"));
        constraints.add(equationFromString(objective.length, "x46 + x47 + x48 + x49 + x50 + x51 + x52 + x53 - x55 = 0"));
        constraints.add(equationFromString(objective.length, "x42 + x43 + x44 + x45 - x40 = 0"));
        constraints.add(equationFromString(objective.length, "x46 + x47 + x48 + x49 + x50 + x51 + x52 + x53 - x41 = 0"));
        constraints.add(equationFromString(objective.length, "x56 + x57 + x58 + x59 - x68 = 0"));
        constraints.add(equationFromString(objective.length, "x60 + x61 + x62 + x63 + x64 + x65 + x66 + x67 - x69 = 0"));
        constraints.add(equationFromString(objective.length, "x60 + x61 + x62 + x63 + x64 + x65 + x66 + x67 >= 51"));
        constraints.add(equationFromString(objective.length, "x56 + x57 + x58 + x59 >= 44"));
        constraints.add(equationFromString(objective.length, "x70 + x71 + x72 + x73 - x82 = 0"));
        constraints.add(equationFromString(objective.length, "x74 + x75 + x76 + x77 + x78 + x79 + x80 + x81 - x83 = 0"));
        constraints.add(equationFromString(objective.length, "x70 + x71 + x72 + x73 - x68 = 0"));
        constraints.add(equationFromString(objective.length, "x74 + x75 + x76 + x77 + x78 + x79 + x80 + x81 - x69 = 0"));
        constraints.add(equationFromString(objective.length, "x84 + x85 + x86 + x87 - x96 = 0"));
        constraints.add(equationFromString(objective.length, "x88 + x89 + x90 + x91 + x92 + x93 + x94 + x95 - x97 = 0"));
        constraints.add(equationFromString(objective.length, "x88 + x89 + x90 + x91 + x92 + x93 + x94 + x95 >= 51"));
        constraints.add(equationFromString(objective.length, "x84 + x85 + x86 + x87 >= 44"));
        constraints.add(equationFromString(objective.length, "x98 + x99 + x100 + x101 - x110 = 0"));
        constraints.add(equationFromString(objective.length, "x102 + x103 + x104 + x105 + x106 + x107 + x108 + x109 - x111 = 0"));
        constraints.add(equationFromString(objective.length, "x98 + x99 + x100 + x101 - x96 = 0"));
        constraints.add(equationFromString(objective.length, "x102 + x103 + x104 + x105 + x106 + x107 + x108 + x109 - x97 = 0"));
        constraints.add(equationFromString(objective.length, "x112 + x113 + x114 + x115 - x124 = 0"));
        constraints.add(equationFromString(objective.length, "x116 + x117 + x118 + x119 + x120 + x121 + x122 + x123 - x125 = 0"));
        constraints.add(equationFromString(objective.length, "x116 + x117 + x118 + x119 + x120 + x121 + x122 + x123 >= 49"));
        constraints.add(equationFromString(objective.length, "x112 + x113 + x114 + x115 >= 42"));
        constraints.add(equationFromString(objective.length, "x126 + x127 + x128 + x129 - x138 = 0"));
        constraints.add(equationFromString(objective.length, "x130 + x131 + x132 + x133 + x134 + x135 + x136 + x137 - x139 = 0"));
        constraints.add(equationFromString(objective.length, "x126 + x127 + x128 + x129 - x124 = 0"));
        constraints.add(equationFromString(objective.length, "x130 + x131 + x132 + x133 + x134 + x135 + x136 + x137 - x125 = 0"));
        constraints.add(equationFromString(objective.length, "x140 + x141 + x142 + x143 - x152 = 0"));
        constraints.add(equationFromString(objective.length, "x144 + x145 + x146 + x147 + x148 + x149 + x150 + x151 - x153 = 0"));
        constraints.add(equationFromString(objective.length, "x144 + x145 + x146 + x147 + x148 + x149 + x150 + x151 >= 59"));
        constraints.add(equationFromString(objective.length, "x140 + x141 + x142 + x143 >= 42"));
        constraints.add(equationFromString(objective.length, "x154 + x155 + x156 + x157 - x166 = 0"));
        constraints.add(equationFromString(objective.length, "x158 + x159 + x160 + x161 + x162 + x163 + x164 + x165 - x167 = 0"));
        constraints.add(equationFromString(objective.length, "x154 + x155 + x156 + x157 - x152 = 0"));
        constraints.add(equationFromString(objective.length, "x158 + x159 + x160 + x161 + x162 + x163 + x164 + x165 - x153 = 0"));
        constraints.add(equationFromString(objective.length, "x83 + x82 - x168 = 0"));
        constraints.add(equationFromString(objective.length, "x111 + x110 - x169 = 0"));
        constraints.add(equationFromString(objective.length, "x170 - x182 = 0"));
        constraints.add(equationFromString(objective.length, "x171 - x183 = 0"));
        constraints.add(equationFromString(objective.length, "x172 - x184 = 0"));
        constraints.add(equationFromString(objective.length, "x173 - x185 = 0"));
        constraints.add(equationFromString(objective.length, "x174 - x186 = 0"));
        constraints.add(equationFromString(objective.length, "x175 + x176 - x187 = 0"));
        constraints.add(equationFromString(objective.length, "x177 - x188 = 0"));
        constraints.add(equationFromString(objective.length, "x178 - x189 = 0"));
        constraints.add(equationFromString(objective.length, "x179 - x190 = 0"));
        constraints.add(equationFromString(objective.length, "x180 - x191 = 0"));
        constraints.add(equationFromString(objective.length, "x181 - x192 = 0"));
        constraints.add(equationFromString(objective.length, "x170 - x26 = 0"));
        constraints.add(equationFromString(objective.length, "x171 - x27 = 0"));
        constraints.add(equationFromString(objective.length, "x172 - x54 = 0"));
        constraints.add(equationFromString(objective.length, "x173 - x55 = 0"));
        constraints.add(equationFromString(objective.length, "x174 - x168 = 0"));
        constraints.add(equationFromString(objective.length, "x177 - x169 = 0"));
        constraints.add(equationFromString(objective.length, "x178 - x138 = 0"));
        constraints.add(equationFromString(objective.length, "x179 - x139 = 0"));
        constraints.add(equationFromString(objective.length, "x180 - x166 = 0"));
        constraints.add(equationFromString(objective.length, "x181 - x167 = 0"));
        constraints.add(equationFromString(objective.length, "x193 - x205 = 0"));
        constraints.add(equationFromString(objective.length, "x194 - x206 = 0"));
        constraints.add(equationFromString(objective.length, "x195 - x207 = 0"));
        constraints.add(equationFromString(objective.length, "x196 - x208 = 0"));
        constraints.add(equationFromString(objective.length, "x197 - x209 = 0"));
        constraints.add(equationFromString(objective.length, "x198 + x199 - x210 = 0"));
        constraints.add(equationFromString(objective.length, "x200 - x211 = 0"));
        constraints.add(equationFromString(objective.length, "x201 - x212 = 0"));
        constraints.add(equationFromString(objective.length, "x202 - x213 = 0"));
        constraints.add(equationFromString(objective.length, "x203 - x214 = 0"));
        constraints.add(equationFromString(objective.length, "x204 - x215 = 0"));
        constraints.add(equationFromString(objective.length, "x193 - x182 = 0"));
        constraints.add(equationFromString(objective.length, "x194 - x183 = 0"));
        constraints.add(equationFromString(objective.length, "x195 - x184 = 0"));
        constraints.add(equationFromString(objective.length, "x196 - x185 = 0"));
        constraints.add(equationFromString(objective.length, "x197 - x186 = 0"));
        constraints.add(equationFromString(objective.length, "x198 + x199 - x187 = 0"));
        constraints.add(equationFromString(objective.length, "x200 - x188 = 0"));
        constraints.add(equationFromString(objective.length, "x201 - x189 = 0"));
        constraints.add(equationFromString(objective.length, "x202 - x190 = 0"));
        constraints.add(equationFromString(objective.length, "x203 - x191 = 0"));
        constraints.add(equationFromString(objective.length, "x204 - x192 = 0"));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(DEFAULT_MAX_ITER, f, new LinearConstraintSet(constraints),
                                                  GoalType.MINIMIZE, new NonNegativeConstraint(true));
        Assert.assertEquals(7518.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizerTest::testCircleFitting
    public void testCircleFitting() {
        CircleScalar circle = new CircleScalar();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        
        
        
        GradientMultivariateOptimizer underlying
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-10, 1e-10));
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(753289573253l);
        RandomVectorGenerator generator
            = new UncorrelatedRandomVectorGenerator(new double[] { 50, 50 },
                                                    new double[] { 10, 10 },
                                                    new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer
            = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(200),
                                 circle.getObjectiveFunction(),
                                 circle.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 98.680, 47.345 }));
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        PointValuePair[] optima = optimizer.getOptima();
        for (PointValuePair o : optima) {
            Vector2D center = new Vector2D(o.getPointRef()[0], o.getPointRef()[1]);
            Assert.assertEquals(69.960161753, circle.getRadius(center), 1e-8);
            Assert.assertEquals(96.075902096, center.getX(), 1e-8);
            Assert.assertEquals(48.135167894, center.getY(), 1e-8);
        }
        Assert.assertTrue(optimizer.getEvaluations() > 70);
        Assert.assertTrue(optimizer.getEvaluations() < 90);
        Assert.assertEquals(3.1267527, optimum.getValue(), 1e-8);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizerTest::testRosenbrock
    public void testRosenbrock() {
        Rosenbrock rosenbrock = new Rosenbrock();
        SimplexOptimizer underlying
            = new SimplexOptimizer(new SimpleValueChecker(-1, 1e-3));
        NelderMeadSimplex simplex = new NelderMeadSimplex(new double[][] {
                { -1.2,  1.0 },
                { 0.9, 1.2 } ,
                {  3.5, -2.3 }
            });
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(16069223052l);
        RandomVectorGenerator generator
            = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
        MultiStartMultivariateOptimizer optimizer
            = new MultiStartMultivariateOptimizer(underlying, 10, generator);
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(1100),
                                 new ObjectiveFunction(rosenbrock),
                                 GoalType.MINIMIZE,
                                 simplex,
                                 new InitialGuess(new double[] { -1.2, 1.0 }));

        Assert.assertEquals(rosenbrock.getCount(), optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 900);
        Assert.assertTrue(optimizer.getEvaluations() < 1200);
        Assert.assertTrue(optimum.getValue() < 8e-4);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionMappingAdapterTest::testStartSimplexInsideRange
    public void testStartSimplexInsideRange() {
        final BiQuadratic biQuadratic = new BiQuadratic(2.0, 2.5, 1.0, 3.0, 2.0, 3.0);
        final MultivariateFunctionMappingAdapter wrapped
            = new MultivariateFunctionMappingAdapter(biQuadratic,
                                                     biQuadratic.getLower(),
                                                     biQuadratic.getUpper());

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final AbstractSimplex simplex = new NelderMeadSimplex(new double[][] {
                wrapped.boundedToUnbounded(new double[] { 1.5, 2.75 }),
                wrapped.boundedToUnbounded(new double[] { 1.5, 2.95 }),
                wrapped.boundedToUnbounded(new double[] { 1.7, 2.90 })
            });

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(300),
                                 new ObjectiveFunction(wrapped),
                                 simplex,
                                 GoalType.MINIMIZE,
                                 new InitialGuess(wrapped.boundedToUnbounded(new double[] { 1.5, 2.25 })));
        final double[] bounded = wrapped.unboundedToBounded(optimum.getPoint());

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), bounded[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), bounded[1], 2e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionMappingAdapterTest::testOptimumOutsideRange
    public void testOptimumOutsideRange() {
        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 0.0, 1.0, 3.0, 2.0, 3.0);
        final MultivariateFunctionMappingAdapter wrapped
            = new MultivariateFunctionMappingAdapter(biQuadratic,
                                                     biQuadratic.getLower(),
                                                     biQuadratic.getUpper());

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final AbstractSimplex simplex = new NelderMeadSimplex(new double[][] {
                wrapped.boundedToUnbounded(new double[] { 1.5, 2.75 }),
                wrapped.boundedToUnbounded(new double[] { 1.5, 2.95 }),
                wrapped.boundedToUnbounded(new double[] { 1.7, 2.90 })
            });

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 new ObjectiveFunction(wrapped),
                                 simplex,
                                 GoalType.MINIMIZE,
                                 new InitialGuess(wrapped.boundedToUnbounded(new double[] { 1.5, 2.25 })));
        final double[] bounded = wrapped.unboundedToBounded(optimum.getPoint());

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), bounded[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), bounded[1], 2e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionMappingAdapterTest::testUnbounded
    public void testUnbounded() {
        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 0.0,
                                                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                                                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        final MultivariateFunctionMappingAdapter wrapped
            = new MultivariateFunctionMappingAdapter(biQuadratic,
                                                     biQuadratic.getLower(),
                                                     biQuadratic.getUpper());

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final AbstractSimplex simplex = new NelderMeadSimplex(new double[][] {
                wrapped.boundedToUnbounded(new double[] { 1.5, 2.75 }),
                wrapped.boundedToUnbounded(new double[] { 1.5, 2.95 }),
                wrapped.boundedToUnbounded(new double[] { 1.7, 2.90 })
            });

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(300),
                                 new ObjectiveFunction(wrapped),
                                 simplex,
                                 GoalType.MINIMIZE,
                                 new InitialGuess(wrapped.boundedToUnbounded(new double[] { 1.5, 2.25 })));
        final double[] bounded = wrapped.unboundedToBounded(optimum.getPoint());

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), bounded[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), bounded[1], 2e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionMappingAdapterTest::testHalfBounded
    public void testHalfBounded() {
        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 4.0,
                                                        1.0, Double.POSITIVE_INFINITY,
                                                        Double.NEGATIVE_INFINITY, 3.0);
        final MultivariateFunctionMappingAdapter wrapped
            = new MultivariateFunctionMappingAdapter(biQuadratic,
                                                     biQuadratic.getLower(),
                                                     biQuadratic.getUpper());

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-13, 1e-30);
        final AbstractSimplex simplex = new NelderMeadSimplex(new double[][] {
                wrapped.boundedToUnbounded(new double[] { 1.5, 2.75 }),
                wrapped.boundedToUnbounded(new double[] { 1.5, 2.95 }),
                wrapped.boundedToUnbounded(new double[] { 1.7, 2.90 })
            });

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(200),
                                 new ObjectiveFunction(wrapped),
                                 simplex,
                                 GoalType.MINIMIZE,
                                 new InitialGuess(wrapped.boundedToUnbounded(new double[] { 1.5, 2.25 })));
        final double[] bounded = wrapped.unboundedToBounded(optimum.getPoint());

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), bounded[0], 1e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), bounded[1], 1e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapterTest::testStartSimplexInsideRange
    public void testStartSimplexInsideRange() {
        final BiQuadratic biQuadratic = new BiQuadratic(2.0, 2.5, 1.0, 3.0, 2.0, 3.0);
        final MultivariateFunctionPenaltyAdapter wrapped
              = new MultivariateFunctionPenaltyAdapter(biQuadratic,
                                                       biQuadratic.getLower(),
                                                       biQuadratic.getUpper(),
                                                       1000.0, new double[] { 100.0, 100.0 });

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final AbstractSimplex simplex = new NelderMeadSimplex(new double[] { 1.0, 0.5 });

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(300),
                                 new ObjectiveFunction(wrapped),
                                 simplex,
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 1.5, 2.25 }));

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), optimum.getPoint()[1], 2e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapterTest::testStartSimplexOutsideRange
    public void testStartSimplexOutsideRange() {
        final BiQuadratic biQuadratic = new BiQuadratic(2.0, 2.5, 1.0, 3.0, 2.0, 3.0);
        final MultivariateFunctionPenaltyAdapter wrapped
              = new MultivariateFunctionPenaltyAdapter(biQuadratic,
                                                       biQuadratic.getLower(),
                                                       biQuadratic.getUpper(),
                                                       1000.0, new double[] { 100.0, 100.0 });

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final AbstractSimplex simplex = new NelderMeadSimplex(new double[] { 1.0, 0.5 });

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(300),
                                 new ObjectiveFunction(wrapped),
                                 simplex,
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { -1.5, 4.0 }));

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), optimum.getPoint()[1], 2e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapterTest::testOptimumOutsideRange
    public void testOptimumOutsideRange() {
        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 0.0, 1.0, 3.0, 2.0, 3.0);
        final MultivariateFunctionPenaltyAdapter wrapped
            =  new MultivariateFunctionPenaltyAdapter(biQuadratic,
                                                      biQuadratic.getLower(),
                                                      biQuadratic.getUpper(),
                                                      1000.0, new double[] { 100.0, 100.0 });

        SimplexOptimizer optimizer = new SimplexOptimizer(new SimplePointChecker<PointValuePair>(1.0e-11, 1.0e-20));
        final AbstractSimplex simplex = new NelderMeadSimplex(new double[] { 1.0, 0.5 });

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(600),
                                 new ObjectiveFunction(wrapped),
                                 simplex,
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { -1.5, 4.0 }));

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), optimum.getPoint()[1], 2e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapterTest::testUnbounded
    public void testUnbounded() {
        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 0.0,
                                                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                                                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        final MultivariateFunctionPenaltyAdapter wrapped
            = new MultivariateFunctionPenaltyAdapter(biQuadratic,
                                                     biQuadratic.getLower(),
                                                     biQuadratic.getUpper(),
                                                     1000.0, new double[] { 100.0, 100.0 });

        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final AbstractSimplex simplex = new NelderMeadSimplex(new double[] { 1.0, 0.5 });

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(300),
                                 new ObjectiveFunction(wrapped),
                                 simplex,
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { -1.5, 4.0 }));

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), optimum.getPoint()[1], 2e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.MultivariateFunctionPenaltyAdapterTest::testHalfBounded
    public void testHalfBounded() {
        final BiQuadratic biQuadratic = new BiQuadratic(4.0, 4.0,
                                                        1.0, Double.POSITIVE_INFINITY,
                                                        Double.NEGATIVE_INFINITY, 3.0);
        final MultivariateFunctionPenaltyAdapter wrapped
              = new MultivariateFunctionPenaltyAdapter(biQuadratic,
                                                       biQuadratic.getLower(),
                                                       biQuadratic.getUpper(),
                                                       1000.0, new double[] { 100.0, 100.0 });

        SimplexOptimizer optimizer = new SimplexOptimizer(new SimplePointChecker<PointValuePair>(1.0e-10, 1.0e-20));
        final AbstractSimplex simplex = new NelderMeadSimplex(new double[] { 1.0, 0.5 });

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(400),
                                 new ObjectiveFunction(wrapped),
                                 simplex,
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { -1.5, 4.0 }));

        Assert.assertEquals(biQuadratic.getBoundedXOptimum(), optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(biQuadratic.getBoundedYOptimum(), optimum.getPoint()[1], 2e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testBoundsUnsupported
    public void testBoundsUnsupported() {
        LinearProblem problem
            = new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        NonLinearConjugateGradientOptimizer optimizer
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-6, 1e-6));
        optimizer.optimize(new MaxEval(100),
                           problem.getObjectiveFunction(),
                           problem.getObjectiveFunctionGradient(),
                           GoalType.MINIMIZE,
                           new InitialGuess(new double[] { 0 }),
                           new SimpleBounds(new double[] { -1 },
                                            new double[] { 1 }));
    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testTrivial
    public void testTrivial() {
        LinearProblem problem
            = new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        NonLinearConjugateGradientOptimizer optimizer
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 0 }));
        Assert.assertEquals(1.5, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-10);

        
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testColumnsPermutation
    public void testColumnsPermutation() {
        LinearProblem problem
            = new LinearProblem(new double[][] { { 1.0, -1.0 }, { 0.0, 2.0 }, { 1.0, -2.0 } },
                                new double[] { 4.0, 6.0, 1.0 });

        NonLinearConjugateGradientOptimizer optimizer
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 0, 0 }));
        Assert.assertEquals(7.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(0.0, optimum.getValue(), 1.0e-10);

    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testNoDependency
    public void testNoDependency() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 2, 0, 0, 0, 0, 0 },
                { 0, 2, 0, 0, 0, 0 },
                { 0, 0, 2, 0, 0, 0 },
                { 0, 0, 0, 2, 0, 0 },
                { 0, 0, 0, 0, 2, 0 },
                { 0, 0, 0, 0, 0, 2 }
        }, new double[] { 0.0, 1.1, 2.2, 3.3, 4.4, 5.5 });
        NonLinearConjugateGradientOptimizer optimizer
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 0, 0, 0, 0, 0, 0 }));
        for (int i = 0; i < problem.target.length; ++i) {
            Assert.assertEquals(0.55 * i, optimum.getPoint()[i], 1.0e-10);
        }
    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testOneSet
    public void testOneSet() {
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1,  0, 0 },
                { -1,  1, 0 },
                {  0, -1, 1 }
        }, new double[] { 1, 1, 1});
        NonLinearConjugateGradientOptimizer optimizer
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 0, 0, 0 }));
        Assert.assertEquals(1.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(3.0, optimum.getPoint()[2], 1.0e-10);

    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testTwoSets
    public void testTwoSets() {
        final double epsilon = 1.0e-7;
        LinearProblem problem = new LinearProblem(new double[][] {
                {  2,  1,   0,  4,       0, 0 },
                { -4, -2,   3, -7,       0, 0 },
                {  4,  1,  -2,  8,       0, 0 },
                {  0, -3, -12, -1,       0, 0 },
                {  0,  0,   0,  0, epsilon, 1 },
                {  0,  0,   0,  0,       1, 1 }
        }, new double[] { 2, -9, 2, 2, 1 + epsilon * epsilon, 2});

        final Preconditioner preconditioner
            = new Preconditioner() {
                    public double[] precondition(double[] point, double[] r) {
                        double[] d = r.clone();
                        d[0] /=  72.0;
                        d[1] /=  30.0;
                        d[2] /= 314.0;
                        d[3] /= 260.0;
                        d[4] /= 2 * (1 + epsilon * epsilon);
                        d[5] /= 4.0;
                        return d;
                    }
                };

        NonLinearConjugateGradientOptimizer optimizer
           = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                     new SimpleValueChecker(1e-13, 1e-13),
                                                     new BrentSolver(),
                                                     preconditioner);

        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 0, 0, 0, 0, 0, 0 }));
        Assert.assertEquals( 3.0, optimum.getPoint()[0], 1.0e-10);
        Assert.assertEquals( 4.0, optimum.getPoint()[1], 1.0e-10);
        Assert.assertEquals(-1.0, optimum.getPoint()[2], 1.0e-10);
        Assert.assertEquals(-2.0, optimum.getPoint()[3], 1.0e-10);
        Assert.assertEquals( 1.0 + epsilon, optimum.getPoint()[4], 1.0e-10);
        Assert.assertEquals( 1.0 - epsilon, optimum.getPoint()[5], 1.0e-10);

    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testNonInversible
    public void testNonInversible() {
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });
        NonLinearConjugateGradientOptimizer optimizer
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 0, 0, 0 }));
        Assert.assertTrue(optimum.getValue() > 0.5);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testIllConditioned
    public void testIllConditioned() {
        LinearProblem problem1 = new LinearProblem(new double[][] {
                { 10.0, 7.0,  8.0,  7.0 },
                {  7.0, 5.0,  6.0,  5.0 },
                {  8.0, 6.0, 10.0,  9.0 },
                {  7.0, 5.0,  9.0, 10.0 }
        }, new double[] { 32, 23, 33, 31 });
        NonLinearConjugateGradientOptimizer optimizer
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-13, 1e-13),
                                                      new BrentSolver(1e-15, 1e-15));
        PointValuePair optimum1
            = optimizer.optimize(new MaxEval(200),
                                 problem1.getObjectiveFunction(),
                                 problem1.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 0, 1, 2, 3 }));
        Assert.assertEquals(1.0, optimum1.getPoint()[0], 1.0e-4);
        Assert.assertEquals(1.0, optimum1.getPoint()[1], 1.0e-4);
        Assert.assertEquals(1.0, optimum1.getPoint()[2], 1.0e-4);
        Assert.assertEquals(1.0, optimum1.getPoint()[3], 1.0e-4);

        LinearProblem problem2 = new LinearProblem(new double[][] {
                { 10.00, 7.00, 8.10, 7.20 },
                {  7.08, 5.04, 6.00, 5.00 },
                {  8.00, 5.98, 9.89, 9.00 },
                {  6.99, 4.99, 9.00, 9.98 }
        }, new double[] { 32, 23, 33, 31 });
        PointValuePair optimum2
            = optimizer.optimize(new MaxEval(200),
                                 problem2.getObjectiveFunction(),
                                 problem2.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 0, 1, 2, 3 }));
        Assert.assertEquals(-81.0, optimum2.getPoint()[0], 1.0e-1);
        Assert.assertEquals(137.0, optimum2.getPoint()[1], 1.0e-1);
        Assert.assertEquals(-34.0, optimum2.getPoint()[2], 1.0e-1);
        Assert.assertEquals( 22.0, optimum2.getPoint()[3], 1.0e-1);

    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 3.0, 2.0,  0.0, 0.0 },
                { 0.0, 1.0, -1.0, 1.0 },
                { 2.0, 0.0,  1.0, 0.0 }
        }, new double[] { 7.0, 3.0, 5.0 });

        NonLinearConjugateGradientOptimizer optimizer
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 7, 6, 5, 4 }));
        Assert.assertEquals(0, optimum.getValue(), 1.0e-10);

    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() {
        LinearProblem problem = new LinearProblem(new double[][] {
                 { 1.0, 1.0,  0.0,  0.0, 0.0,  0.0 },
                 { 0.0, 0.0,  1.0,  1.0, 1.0,  0.0 },
                 { 0.0, 0.0,  0.0,  0.0, 1.0, -1.0 },
                 { 0.0, 0.0, -1.0,  1.0, 0.0,  1.0 },
                 { 0.0, 0.0,  0.0, -1.0, 1.0,  0.0 }
        }, new double[] { 3.0, 12.0, -1.0, 7.0, 1.0 });
        NonLinearConjugateGradientOptimizer optimizer
           = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                     new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 2, 2, 2, 2, 2, 2 }));
        Assert.assertEquals(0, optimum.getValue(), 1.0e-10);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testRedundantEquations
    public void testRedundantEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 5.0 });

        NonLinearConjugateGradientOptimizer optimizer
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 1, 1 }));
        Assert.assertEquals(2.0, optimum.getPoint()[0], 1.0e-8);
        Assert.assertEquals(1.0, optimum.getPoint()[1], 1.0e-8);

    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testInconsistentEquations
    public void testInconsistentEquations() {
        LinearProblem problem = new LinearProblem(new double[][] {
                { 1.0,  1.0 },
                { 1.0, -1.0 },
                { 1.0,  3.0 }
        }, new double[] { 3.0, 1.0, 4.0 });

        NonLinearConjugateGradientOptimizer optimizer
            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                      new SimpleValueChecker(1e-6, 1e-6));
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 1, 1 }));
        Assert.assertTrue(optimum.getValue() > 0.1);

    }

// org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizerTest::testCircleFitting
    public void testCircleFitting() {
        CircleScalar problem = new CircleScalar();
        problem.addPoint( 30.0,  68.0);
        problem.addPoint( 50.0,  -6.0);
        problem.addPoint(110.0, -20.0);
        problem.addPoint( 35.0,  15.0);
        problem.addPoint( 45.0,  97.0);
        NonLinearConjugateGradientOptimizer optimizer
           = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                     new SimpleValueChecker(1e-30, 1e-30),
                                                     new BrentSolver(1e-15, 1e-13));
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getObjectiveFunction(),
                                 problem.getObjectiveFunctionGradient(),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 98.680, 47.345 }));
        Vector2D center = new Vector2D(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        Assert.assertEquals(69.960161753, problem.getRadius(center), 1.0e-8);
        Assert.assertEquals(96.075902096, center.getX(), 1.0e-8);
        Assert.assertEquals(48.135167894, center.getY(), 1.0e-8);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testInitOutOfBounds
    public void testInitOutOfBounds() {
        double[] startPoint = point(DIM, 3);
        double[][] boundaries = boundaries(DIM, -1, 2);
        doTest(new Rosen(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 2000, null);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testBoundariesDimensionMismatch
    public void testBoundariesDimensionMismatch() {
        double[] startPoint = point(DIM, 0.5);
        double[][] boundaries = boundaries(DIM + 1, -1, 2);
        doTest(new Rosen(), startPoint, boundaries,
               GoalType.MINIMIZE, 
               1e-13, 1e-6, 2000, null);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testProblemDimensionTooSmall
    public void testProblemDimensionTooSmall() {
        double[] startPoint = point(1, 0.5);
        doTest(new Rosen(), startPoint, null,
               GoalType.MINIMIZE,
               1e-13, 1e-6, 2000, null);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testMaxEvaluations
    public void testMaxEvaluations() {
        final int lowMaxEval = 2;
        double[] startPoint = point(DIM, 0.1);
        double[][] boundaries = null;
        doTest(new Rosen(), startPoint, boundaries,
               GoalType.MINIMIZE, 
               1e-13, 1e-6, lowMaxEval, null);
     }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testRosen
    public void testRosen() {
        double[] startPoint = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected = new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 2000, expected);
     }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testMaximize
    public void testMaximize() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected = new PointValuePair(point(DIM,0.0),1.0);
        doTest(new MinusElli(), startPoint, boundaries,
                GoalType.MAXIMIZE, 
                2e-10, 5e-6, 1000, expected);
        boundaries = boundaries(DIM,-0.3,0.3); 
        startPoint = point(DIM,0.1);
        doTest(new MinusElli(), startPoint, boundaries,
                GoalType.MAXIMIZE, 
                2e-10, 5e-6, 1000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testEllipse
    public void testEllipse() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Elli(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 1000, expected);
     }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testElliRotated
    public void testElliRotated() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new ElliRotated(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-12, 1e-6, 10000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testCigar
    public void testCigar() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 100, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testTwoAxes
    public void testTwoAxes() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new TwoAxes(), startPoint, boundaries,
                GoalType.MINIMIZE, 2*
                1e-13, 1e-6, 100, expected);
     }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testCigTab
    public void testCigTab() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new CigTab(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 5e-5, 100, expected);
     }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testSphere
    public void testSphere() {
        double[] startPoint = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Sphere(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 100, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testTablet
    public void testTablet() {
        double[] startPoint = point(DIM,1.0); 
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Tablet(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 100, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testDiffPow
    public void testDiffPow() {}

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testSsDiffPow
    public void testSsDiffPow() {
        double[] startPoint = point(DIM/2,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM/2,0.0),0.0);
        doTest(new SsDiffPow(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-2, 1.3e-1, 50000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testAckley
    public void testAckley() {}

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testRastrigin
    public void testRastrigin() {
        double[] startPoint = point(DIM,1.0);

        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Rastrigin(), startPoint, boundaries,
                GoalType.MINIMIZE, 
                1e-13, 1e-6, 1000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testConstrainedRosen
    public void testConstrainedRosen() {
        double[] startPoint = point(DIM,0.1);

        double[][] boundaries = boundaries(DIM,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, boundaries,
                GoalType.MINIMIZE,
                1e-13, 1e-6, 2000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizerTest::testConstrainedRosenWithMoreInterpolationPoints
    public void testConstrainedRosenWithMoreInterpolationPoints() {
        final double[] startPoint = point(DIM, 0.1);
        final double[][] boundaries = boundaries(DIM, -1, 2);
        final PointValuePair expected = new PointValuePair(point(DIM, 1.0), 0.0);

        
        
        
        
        
        final int maxAdditionalPoints = 47;

        for (int num = 1; num <= maxAdditionalPoints; num++) {
            doTest(new Rosen(), startPoint, boundaries,
                   GoalType.MINIMIZE,
                   1e-12, 1e-6, 2000,
                   num,
                   expected,
                   "num=" + num);
        }
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testInitOutofbounds1
    public void testInitOutofbounds1() {
        double[] startPoint = point(DIM,3);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testInitOutofbounds2
    public void testInitOutofbounds2() {
        double[] startPoint = point(DIM, -2);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testBoundariesDimensionMismatch
    public void testBoundariesDimensionMismatch() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM, 0.3);
        double[][] boundaries = boundaries(DIM+1,-1,2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testInputSigmaNegative
    public void testInputSigmaNegative() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM,-0.5);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testInputSigmaOutOfRange
    public void testInputSigmaOutOfRange() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM, 1.1);
        double[][] boundaries = boundaries(DIM,-0.5,0.5);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testInputSigmaDimensionMismatch
    public void testInputSigmaDimensionMismatch() {
        double[] startPoint = point(DIM,0.5);
        double[] insigma = point(DIM + 1, 0.5);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testRosen
    public void testRosen() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testMaximize
    public void testMaximize() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),1.0);
        doTest(new MinusElli(), startPoint, insigma, boundaries,
                GoalType.MAXIMIZE, LAMBDA, true, 0, 1.0-1e-13,
                2e-10, 5e-6, 100000, expected);
        doTest(new MinusElli(), startPoint, insigma, boundaries,
                GoalType.MAXIMIZE, LAMBDA, false, 0, 1.0-1e-13,
                2e-10, 5e-6, 100000, expected);
        boundaries = boundaries(DIM,-0.3,0.3); 
        startPoint = point(DIM,0.1);
        doTest(new MinusElli(), startPoint, insigma, boundaries,
                GoalType.MAXIMIZE, LAMBDA, true, 0, 1.0-1e-13,
                2e-10, 5e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testEllipse
    public void testEllipse() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Elli(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Elli(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testElliRotated
    public void testElliRotated() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new ElliRotated(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new ElliRotated(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testCigar
    public void testCigar() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testCigarWithBoundaries
    public void testCigarWithBoundaries() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = boundaries(DIM, -1e100, Double.POSITIVE_INFINITY);
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Cigar(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testTwoAxes
    public void testTwoAxes() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new TwoAxes(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new TwoAxes(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-8, 1e-3, 200000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testCigTab
    public void testCigTab() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.3);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new CigTab(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 5e-5, 100000, expected);
        doTest(new CigTab(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 5e-5, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testSphere
    public void testSphere() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Sphere(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Sphere(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testTablet
    public void testTablet() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Tablet(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Tablet(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testDiffPow
    public void testDiffPow() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new DiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, true, 0, 1e-13,
                1e-8, 1e-1, 100000, expected);
        doTest(new DiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, false, 0, 1e-13,
                1e-8, 2e-1, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testSsDiffPow
    public void testSsDiffPow() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new SsDiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, true, 0, 1e-13,
                1e-4, 1e-1, 200000, expected);
        doTest(new SsDiffPow(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 10, false, 0, 1e-13,
                1e-4, 1e-1, 200000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testAckley
    public void testAckley() {
        double[] startPoint = point(DIM,1.0);
        double[] insigma = point(DIM,1.0);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Ackley(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-9, 1e-5, 100000, expected);
        doTest(new Ackley(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-9, 1e-5, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testRastrigin
    public void testRastrigin() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,0.0),0.0);
        doTest(new Rastrigin(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, (int)(200*Math.sqrt(DIM)), true, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
        doTest(new Rastrigin(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, (int)(200*Math.sqrt(DIM)), false, 0, 1e-13,
                1e-13, 1e-6, 200000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testConstrainedRosen
    public void testConstrainedRosen() {
        double[] startPoint = point(DIM, 0.1);
        double[] insigma = point(DIM, 0.1);
        double[][] boundaries = boundaries(DIM, -1, 2);
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, true, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, 2*LAMBDA, false, 0, 1e-13,
                1e-13, 1e-6, 100000, expected);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testDiagonalRosen
    public void testDiagonalRosen() {
        double[] startPoint = point(DIM,0.1);
        double[] insigma = point(DIM,0.1);
        double[][] boundaries = null;
        PointValuePair expected =
            new PointValuePair(point(DIM,1.0),0.0);
        doTest(new Rosen(), startPoint, insigma, boundaries,
                GoalType.MINIMIZE, LAMBDA, false, 1, 1e-13,
                1e-10, 1e-4, 1000000, expected);
     }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testMath864
    public void testMath864() {
        final CMAESOptimizer optimizer
            = new CMAESOptimizer(30000, 0, true, 10,
                                 0, new MersenneTwister(), false, null);
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 1;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 0 };
        final double[] lower = { -1e6 };
        final double[] upper = { 1.5 };
        final double[] sigma = { 1e-1 };
        final double[] result = optimizer.optimize(new MaxEval(10000),
                                                   new ObjectiveFunction(fitnessFunction),
                                                   GoalType.MINIMIZE,
                                                   new CMAESOptimizer.PopulationSize(5),
                                                   new CMAESOptimizer.Sigma(sigma),
                                                   new InitialGuess(start),
                                                   new SimpleBounds(lower, upper)).getPoint();
        Assert.assertTrue("Out of bounds (" + result[0] + " > " + upper[0] + ")",
                          result[0] <= upper[0]);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizerTest::testFitAccuracyDependsOnBoundary
    public void testFitAccuracyDependsOnBoundary() {
        final CMAESOptimizer optimizer
            = new CMAESOptimizer(30000, 0, true, 10,
                                 0, new MersenneTwister(), false, null);
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 11.1;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 1 };
 
        
        PointValuePair result = optimizer.optimize(new MaxEval(100000),
                                                   new ObjectiveFunction(fitnessFunction),
                                                   GoalType.MINIMIZE,
                                                   SimpleBounds.unbounded(1),
                                                   new CMAESOptimizer.PopulationSize(5),
                                                   new CMAESOptimizer.Sigma(new double[] { 1e-1 }),
                                                   new InitialGuess(start));
        final double resNoBound = result.getPoint()[0];

        
        final double[] lower = { -20 };
        final double[] upper = { 5e16 };
        final double[] sigma = { 10 };
        result = optimizer.optimize(new MaxEval(100000),
                                    new ObjectiveFunction(fitnessFunction),
                                    GoalType.MINIMIZE,
                                    new CMAESOptimizer.PopulationSize(5),
                                    new CMAESOptimizer.Sigma(sigma),
                                    new InitialGuess(start),
                                    new SimpleBounds(lower, upper));
        final double resNearLo = result.getPoint()[0];

        
        lower[0] = -5e16;
        upper[0] = 20;
        result = optimizer.optimize(new MaxEval(100000),
                                    new ObjectiveFunction(fitnessFunction),
                                    GoalType.MINIMIZE,
                                    new CMAESOptimizer.PopulationSize(5),
                                    new CMAESOptimizer.Sigma(sigma),
                                    new InitialGuess(start),
                                    new SimpleBounds(lower, upper));
        final double resNearHi = result.getPoint()[0];

        
        
        

        
        
        Assert.assertEquals(resNoBound, resNearLo, 1e-3);
        Assert.assertEquals(resNoBound, resNearHi, 1e-3);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest::testBoundsUnsupported
    public void testBoundsUnsupported() {
        final MultivariateFunction func = new SumSincFunction(-1);
        final PowellOptimizer optim = new PowellOptimizer(1e-8, 1e-5,
                                                          1e-4, 1e-4);

        optim.optimize(new MaxEval(100),
                       new ObjectiveFunction(func),
                       GoalType.MINIMIZE,
                       new InitialGuess(new double[] { -3, 0 }),
                       new SimpleBounds(new double[] { -5, -1 },
                                        new double[] { 5, 1 }));
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest::testSumSinc
    public void testSumSinc() {
        final MultivariateFunction func = new SumSincFunction(-1);

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 0;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i];
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-9);

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] + 3;
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-5);
        
        
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-9, 1e-7);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest::testQuadratic
    public void testQuadratic() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return a * a + b * b + 1;
                }
            };

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 1;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i];
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-8);

        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] - 20;
        }
        doTest(func, minPoint, init, GoalType.MINIMIZE, 1e-9, 1e-8);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest::testMaximizeQuadratic
    public void testMaximizeQuadratic() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return -a * a - b * b + 1;
                }
            };

        int dim = 2;
        final double[] maxPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            maxPoint[i] = 1;
        }

        double[] init = new double[dim];

        
        for (int i = 0; i < dim; i++) {
            init[i] = maxPoint[i];
        }
        doTest(func, maxPoint, init,  GoalType.MAXIMIZE, 1e-9, 1e-8);

        
        for (int i = 0; i < dim; i++) {
            init[i] = maxPoint[i] - 20;
        }
        doTest(func, maxPoint, init, GoalType.MAXIMIZE, 1e-9, 1e-8);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizerTest::testRelativeToleranceOnScaledValues
    public void testRelativeToleranceOnScaledValues() {
        final MultivariateFunction func = new MultivariateFunction() {
                public double value(double[] x) {
                    final double a = x[0] - 1;
                    final double b = x[1] - 1;
                    return a * a * FastMath.sqrt(FastMath.abs(a)) + b * b + 1;
                }
            };

        int dim = 2;
        final double[] minPoint = new double[dim];
        for (int i = 0; i < dim; i++) {
            minPoint[i] = 1;
        }

        double[] init = new double[dim];
        
        for (int i = 0; i < dim; i++) {
            init[i] = minPoint[i] - 20;
        }

        final double relTol = 1e-10;

        final int maxEval = 1000;
        
        
        final PowellOptimizer optim = new PowellOptimizer(relTol, 1e-100);

        final PointValuePair funcResult = optim.optimize(new MaxEval(maxEval),
                                                         new ObjectiveFunction(func),
                                                         GoalType.MINIMIZE,
                                                         new InitialGuess(init));
        final double funcValue = func.value(funcResult.getPoint());
        final int funcEvaluations = optim.getEvaluations();

        final double scale = 1e10;
        final MultivariateFunction funcScaled = new MultivariateFunction() {
                public double value(double[] x) {
                    return scale * func.value(x);
                }
            };

        final PointValuePair funcScaledResult = optim.optimize(new MaxEval(maxEval),
                                                               new ObjectiveFunction(funcScaled),
                                                               GoalType.MINIMIZE,
                                                               new InitialGuess(init));
        final double funcScaledValue = funcScaled.value(funcScaledResult.getPoint());
        final int funcScaledEvaluations = optim.getEvaluations();

        
        
        Assert.assertEquals(1, funcScaledValue / (scale * funcValue), relTol);

        
        Assert.assertEquals(funcEvaluations, funcScaledEvaluations);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerMultiDirectionalTest::testBoundsUnsupported
    public void testBoundsUnsupported() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final FourExtrema fourExtrema = new FourExtrema();

        optimizer.optimize(new MaxEval(100),
                           new ObjectiveFunction(fourExtrema),
                           GoalType.MINIMIZE,
                           new InitialGuess(new double[] { -3, 0 }),
                           new NelderMeadSimplex(new double[] { 0.2, 0.2 }),
                           new SimpleBounds(new double[] { -5, -1 },
                                            new double[] { 5, 1 }));
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerMultiDirectionalTest::testMinimize1
    public void testMinimize1() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-11, 1e-30);
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(200),
                                 new ObjectiveFunction(fourExtrema),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { -3, 0 }),
                                 new MultiDirectionalSimplex(new double[] { 0.2, 0.2 }));
        Assert.assertEquals(fourExtrema.xM, optimum.getPoint()[0], 4e-6);
        Assert.assertEquals(fourExtrema.yP, optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(fourExtrema.valueXmYp, optimum.getValue(), 8e-13);
        Assert.assertTrue(optimizer.getEvaluations() > 120);
        Assert.assertTrue(optimizer.getEvaluations() < 150);

        
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerMultiDirectionalTest::testMinimize2
    public void testMinimize2() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-11, 1e-30);
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(200),
                                 new ObjectiveFunction(fourExtrema),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 1, 0 }),
                                 new MultiDirectionalSimplex(new double[] { 0.2, 0.2 }));
        Assert.assertEquals(fourExtrema.xP, optimum.getPoint()[0], 2e-8);
        Assert.assertEquals(fourExtrema.yM, optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(fourExtrema.valueXpYm, optimum.getValue(), 2e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 120);
        Assert.assertTrue(optimizer.getEvaluations() < 150);

        
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerMultiDirectionalTest::testMaximize1
    public void testMaximize1() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-11, 1e-30);
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(200),
                                 new ObjectiveFunction(fourExtrema),
                                 GoalType.MAXIMIZE,
                                 new InitialGuess(new double[] { -3.0, 0.0 }),
                                 new MultiDirectionalSimplex(new double[] { 0.2, 0.2 }));
        Assert.assertEquals(fourExtrema.xM, optimum.getPoint()[0], 7e-7);
        Assert.assertEquals(fourExtrema.yM, optimum.getPoint()[1], 3e-7);
        Assert.assertEquals(fourExtrema.valueXmYm, optimum.getValue(), 2e-14);
        Assert.assertTrue(optimizer.getEvaluations() > 120);
        Assert.assertTrue(optimizer.getEvaluations() < 150);

        
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerMultiDirectionalTest::testMaximize2
    public void testMaximize2() {
        SimplexOptimizer optimizer = new SimplexOptimizer(new SimpleValueChecker(1e-15, 1e-30));
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(200),
                                 new ObjectiveFunction(fourExtrema),
                                 GoalType.MAXIMIZE,
                                 new InitialGuess(new double[] { 1, 0 }),
                                 new MultiDirectionalSimplex(new double[] { 0.2, 0.2 }));
        Assert.assertEquals(fourExtrema.xP, optimum.getPoint()[0], 2e-8);
        Assert.assertEquals(fourExtrema.yP, optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(fourExtrema.valueXpYp, optimum.getValue(), 2e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 180);
        Assert.assertTrue(optimizer.getEvaluations() < 220);

        
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerMultiDirectionalTest::testRosenbrock
    public void testRosenbrock() {
        MultivariateFunction rosenbrock
            = new MultivariateFunction() {
                    public double value(double[] x) {
                        ++count;
                        double a = x[1] - x[0] * x[0];
                        double b = 1.0 - x[0];
                        return 100 * a * a + b * b;
                    }
                };

        count = 0;
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        PointValuePair optimum
           = optimizer.optimize(new MaxEval(100),
                                new ObjectiveFunction(rosenbrock),
                                GoalType.MINIMIZE,
                                new InitialGuess(new double[] { -1.2, 1 }),
                                new MultiDirectionalSimplex(new double[][] {
                                        { -1.2,  1.0 },
                                        { 0.9, 1.2 },
                                        {  3.5, -2.3 } }));

        Assert.assertEquals(count, optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 50);
        Assert.assertTrue(optimizer.getEvaluations() < 100);
        Assert.assertTrue(optimum.getValue() > 1e-2);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerMultiDirectionalTest::testPowell
    public void testPowell() {
        MultivariateFunction powell
            = new MultivariateFunction() {
                    public double value(double[] x) {
                        ++count;
                        double a = x[0] + 10 * x[1];
                        double b = x[2] - x[3];
                        double c = x[1] - 2 * x[2];
                        double d = x[0] - x[3];
                        return a * a + 5 * b * b + c * c * c * c + 10 * d * d * d * d;
                    }
                };

        count = 0;
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(1000),
                                 new ObjectiveFunction(powell),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 3, -1, 0, 1 }),
                                 new MultiDirectionalSimplex(4));
        Assert.assertEquals(count, optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 800);
        Assert.assertTrue(optimizer.getEvaluations() < 900);
        Assert.assertTrue(optimum.getValue() > 1e-2);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerMultiDirectionalTest::testMath283
    public void testMath283() {
        
        
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-14, 1e-14);
        final Gaussian2D function = new Gaussian2D(0, 0, 1);
        PointValuePair estimate = optimizer.optimize(new MaxEval(1000),
                                                     new ObjectiveFunction(function),
                                                     GoalType.MAXIMIZE,
                                                     new InitialGuess(function.getMaximumPosition()),
                                                     new MultiDirectionalSimplex(2));
        final double EPSILON = 1e-5;
        final double expectedMaximum = function.getMaximum();
        final double actualMaximum = estimate.getValue();
        Assert.assertEquals(expectedMaximum, actualMaximum, EPSILON);

        final double[] expectedPosition = function.getMaximumPosition();
        final double[] actualPosition = estimate.getPoint();
        Assert.assertEquals(expectedPosition[0], actualPosition[0], EPSILON );
        Assert.assertEquals(expectedPosition[1], actualPosition[1], EPSILON );
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testBoundsUnsupported
    public void testBoundsUnsupported() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final FourExtrema fourExtrema = new FourExtrema();

        optimizer.optimize(new MaxEval(100),
                           new ObjectiveFunction(fourExtrema),
                           GoalType.MINIMIZE,
                           new InitialGuess(new double[] { -3, 0 }),
                           new NelderMeadSimplex(new double[] { 0.2, 0.2 }),
                           new SimpleBounds(new double[] { -5, -1 },
                                            new double[] { 5, 1 }));
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testMinimize1
    public void testMinimize1() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 new ObjectiveFunction(fourExtrema),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { -3, 0 }),
                                 new NelderMeadSimplex(new double[] { 0.2, 0.2 }));
        Assert.assertEquals(fourExtrema.xM, optimum.getPoint()[0], 2e-7);
        Assert.assertEquals(fourExtrema.yP, optimum.getPoint()[1], 2e-5);
        Assert.assertEquals(fourExtrema.valueXmYp, optimum.getValue(), 6e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 90);

        
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testMinimize2
    public void testMinimize2() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 new ObjectiveFunction(fourExtrema),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 1, 0 }),
                                 new NelderMeadSimplex(new double[] { 0.2, 0.2 }));
        Assert.assertEquals(fourExtrema.xP, optimum.getPoint()[0], 5e-6);
        Assert.assertEquals(fourExtrema.yM, optimum.getPoint()[1], 6e-6);
        Assert.assertEquals(fourExtrema.valueXpYm, optimum.getValue(), 1e-11);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 90);

        
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testMaximize1
    public void testMaximize1() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 new ObjectiveFunction(fourExtrema),
                                 GoalType.MAXIMIZE,
                                 new InitialGuess(new double[] { -3, 0 }),
                                 new NelderMeadSimplex(new double[] { 0.2, 0.2 }));
        Assert.assertEquals(fourExtrema.xM, optimum.getPoint()[0], 1e-5);
        Assert.assertEquals(fourExtrema.yM, optimum.getPoint()[1], 3e-6);
        Assert.assertEquals(fourExtrema.valueXmYm, optimum.getValue(), 3e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 90);

        
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testMaximize2
    public void testMaximize2() {
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-30);
        final FourExtrema fourExtrema = new FourExtrema();

        final PointValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 new ObjectiveFunction(fourExtrema),
                                 GoalType.MAXIMIZE,
                                 new InitialGuess(new double[] { 1, 0 }),
                                 new NelderMeadSimplex(new double[] { 0.2, 0.2 }));
        Assert.assertEquals(fourExtrema.xP, optimum.getPoint()[0], 4e-6);
        Assert.assertEquals(fourExtrema.yP, optimum.getPoint()[1], 5e-6);
        Assert.assertEquals(fourExtrema.valueXpYp, optimum.getValue(), 7e-12);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 90);

        
        Assert.assertTrue(optimizer.getIterations() > 0);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testRosenbrock
    public void testRosenbrock() {

        Rosenbrock rosenbrock = new Rosenbrock();
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        PointValuePair optimum
        = optimizer.optimize(new MaxEval(100),
                             new ObjectiveFunction(rosenbrock),
                             GoalType.MINIMIZE,
                             new InitialGuess(new double[] { -1.2, 1 }),
                                new NelderMeadSimplex(new double[][] {
                                        { -1.2,  1 },
                                        { 0.9, 1.2 },
                                        {  3.5, -2.3 } }));

        Assert.assertEquals(rosenbrock.getCount(), optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 40);
        Assert.assertTrue(optimizer.getEvaluations() < 50);
        Assert.assertTrue(optimum.getValue() < 8e-4);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testPowell
    public void testPowell() {
        Powell powell = new Powell();
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        PointValuePair optimum =
            optimizer.optimize(new MaxEval(200),
                               new ObjectiveFunction(powell),
                               GoalType.MINIMIZE,
                               new InitialGuess(new double[] { 3, -1, 0, 1 }),
                               new NelderMeadSimplex(4));
        Assert.assertEquals(powell.getCount(), optimizer.getEvaluations());
        Assert.assertTrue(optimizer.getEvaluations() > 110);
        Assert.assertTrue(optimizer.getEvaluations() < 130);
        Assert.assertTrue(optimum.getValue() < 2e-3);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testLeastSquares1
    public void testLeastSquares1() {
        final RealMatrix factors
            = new Array2DRowRealMatrix(new double[][] {
                    { 1, 0 },
                    { 0, 1 }
                }, false);
        LeastSquaresConverter ls = new LeastSquaresConverter(new MultivariateVectorFunction() {
                public double[] value(double[] variables) {
                    return factors.operate(variables);
                }
            }, new double[] { 2.0, -3.0 });
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-6);
        PointValuePair optimum =
            optimizer.optimize(new MaxEval(200),
                               new ObjectiveFunction(ls),
                               GoalType.MINIMIZE,
                               new InitialGuess(new double[] { 10, 10 }),
                               new NelderMeadSimplex(2));
        Assert.assertEquals( 2, optimum.getPointRef()[0], 3e-5);
        Assert.assertEquals(-3, optimum.getPointRef()[1], 4e-4);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 80);
        Assert.assertTrue(optimum.getValue() < 1.0e-6);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testLeastSquares2
    public void testLeastSquares2() {
        final RealMatrix factors
            = new Array2DRowRealMatrix(new double[][] {
                    { 1, 0 },
                    { 0, 1 }
                }, false);
        LeastSquaresConverter ls = new LeastSquaresConverter(new MultivariateVectorFunction() {
                public double[] value(double[] variables) {
                    return factors.operate(variables);
                }
            }, new double[] { 2, -3 }, new double[] { 10, 0.1 });
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-6);
        PointValuePair optimum =
            optimizer.optimize(new MaxEval(200),
                               new ObjectiveFunction(ls),
                               GoalType.MINIMIZE,
                               new InitialGuess(new double[] { 10, 10 }),
                               new NelderMeadSimplex(2));
        Assert.assertEquals( 2, optimum.getPointRef()[0], 5e-5);
        Assert.assertEquals(-3, optimum.getPointRef()[1], 8e-4);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 80);
        Assert.assertTrue(optimum.getValue() < 1e-6);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testLeastSquares3
    public void testLeastSquares3() {
        final RealMatrix factors =
            new Array2DRowRealMatrix(new double[][] {
                    { 1, 0 },
                    { 0, 1 }
                }, false);
        LeastSquaresConverter ls = new LeastSquaresConverter(new MultivariateVectorFunction() {
                public double[] value(double[] variables) {
                    return factors.operate(variables);
                }
            }, new double[] { 2, -3 }, new Array2DRowRealMatrix(new double [][] {
                    { 1, 1.2 }, { 1.2, 2 }
                }));
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-6);
        PointValuePair optimum
            = optimizer.optimize(new MaxEval(200),
                                 new ObjectiveFunction(ls),
                                 GoalType.MINIMIZE,
                                 new InitialGuess(new double[] { 10, 10 }),
                                 new NelderMeadSimplex(2));
        Assert.assertEquals( 2, optimum.getPointRef()[0], 2e-3);
        Assert.assertEquals(-3, optimum.getPointRef()[1], 8e-4);
        Assert.assertTrue(optimizer.getEvaluations() > 60);
        Assert.assertTrue(optimizer.getEvaluations() < 80);
        Assert.assertTrue(optimum.getValue() < 1e-6);
    }

// org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizerNelderMeadTest::testMaxIterations
    public void testMaxIterations() {
        Powell powell = new Powell();
        SimplexOptimizer optimizer = new SimplexOptimizer(-1, 1e-3);
        optimizer.optimize(new MaxEval(20),
                           new ObjectiveFunction(powell),
                           GoalType.MINIMIZE,
                           new InitialGuess(new double[] { 3, -1, 0, 1 }),
                           new NelderMeadSimplex(4));
    }

// org.apache.commons.math3.optim.nonlinear.vector.MultiStartMultivariateVectorOptimizerTest::testGetOptimaBeforeOptimize
    public void testGetOptimaBeforeOptimize() {

        JacobianMultivariateVectorOptimizer underlyingOptimizer
            = new GaussNewtonOptimizer(true, new SimpleVectorValueChecker(1e-6, 1e-6));
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(16069223052l);
        RandomVectorGenerator generator
            = new UncorrelatedRandomVectorGenerator(1, new GaussianRandomGenerator(g));
        MultiStartMultivariateVectorOptimizer optimizer
            = new MultiStartMultivariateVectorOptimizer(underlyingOptimizer, 10, generator);

        optimizer.getOptima();
    }

// org.apache.commons.math3.optim.nonlinear.vector.MultiStartMultivariateVectorOptimizerTest::testTrivial
    public void testTrivial() {
        LinearProblem problem
            = new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        JacobianMultivariateVectorOptimizer underlyingOptimizer
            = new GaussNewtonOptimizer(true, new SimpleVectorValueChecker(1e-6, 1e-6));
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(16069223052l);
        RandomVectorGenerator generator
            = new UncorrelatedRandomVectorGenerator(1, new GaussianRandomGenerator(g));
        MultiStartMultivariateVectorOptimizer optimizer
            = new MultiStartMultivariateVectorOptimizer(underlyingOptimizer, 10, generator);

        PointVectorValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getModelFunction(),
                                 problem.getModelFunctionJacobian(),
                                 problem.getTarget(),
                                 new Weight(new double[] { 1 }),
                                 new InitialGuess(new double[] { 0 }));
        Assert.assertEquals(1.5, optimum.getPoint()[0], 1e-10);
        Assert.assertEquals(3.0, optimum.getValue()[0], 1e-10);
        PointVectorValuePair[] optima = optimizer.getOptima();
        Assert.assertEquals(10, optima.length);
        for (int i = 0; i < optima.length; i++) {
            Assert.assertEquals(1.5, optima[i].getPoint()[0], 1e-10);
            Assert.assertEquals(3.0, optima[i].getValue()[0], 1e-10);
        }
        Assert.assertTrue(optimizer.getEvaluations() > 20);
        Assert.assertTrue(optimizer.getEvaluations() < 50);
        Assert.assertEquals(100, optimizer.getMaxEvaluations());
    }

// org.apache.commons.math3.optim.nonlinear.vector.MultiStartMultivariateVectorOptimizerTest::testIssue914
    public void testIssue914() {
        LinearProblem problem = new LinearProblem(new double[][] { { 2 } }, new double[] { 3 });
        JacobianMultivariateVectorOptimizer underlyingOptimizer =
                new GaussNewtonOptimizer(true, new SimpleVectorValueChecker(1e-6, 1e-6)) {
            public PointVectorValuePair optimize(OptimizationData... optData) {
                
                
                OptimizationData[] filtered = optData.clone();
                for (int i = 0; i < filtered.length; ++i) {
                    if (filtered[i] instanceof SimpleBounds) {
                        filtered[i] = null;
                    }
                }
                return super.optimize(filtered);
            }
        };
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(16069223052l);
        RandomVectorGenerator generator =
                new UncorrelatedRandomVectorGenerator(1, new GaussianRandomGenerator(g));
        MultiStartMultivariateVectorOptimizer optimizer =
                new MultiStartMultivariateVectorOptimizer(underlyingOptimizer, 10, generator);

        optimizer.optimize(new MaxEval(100),
                           problem.getModelFunction(),
                           problem.getModelFunctionJacobian(),
                           problem.getTarget(),
                           new Weight(new double[] { 1 }),
                           new InitialGuess(new double[] { 0 }),
                           new SimpleBounds(new double[] { -1.0e-10 }, new double[] {  1.0e-10 }));
        PointVectorValuePair[] optima = optimizer.getOptima();
        
        Assert.assertEquals(1, optima.length);

    }

// org.apache.commons.math3.optim.nonlinear.vector.MultiStartMultivariateVectorOptimizerTest::testNoOptimum
    public void testNoOptimum() {
        JacobianMultivariateVectorOptimizer underlyingOptimizer
            = new GaussNewtonOptimizer(true, new SimpleVectorValueChecker(1e-6, 1e-6));
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(12373523445l);
        RandomVectorGenerator generator
            = new UncorrelatedRandomVectorGenerator(1, new GaussianRandomGenerator(g));
        MultiStartMultivariateVectorOptimizer optimizer
            = new MultiStartMultivariateVectorOptimizer(underlyingOptimizer, 10, generator);
        optimizer.optimize(new MaxEval(100),
                           new Target(new double[] { 0 }),
                           new Weight(new double[] { 1 }),
                           new InitialGuess(new double[] { 0 }),
                           new ModelFunction(new MultivariateVectorFunction() {
                                   public double[] value(double[] point) {
                                       throw new TestException();
                                   }
                               }));
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testConstraintsUnsupported
    public void testConstraintsUnsupported() {
        createOptimizer().optimize(new MaxEval(100),
                                   new Target(new double[] { 2 }),
                                   new Weight(new double[] { 1 }),
                                   new InitialGuess(new double[] { 1, 2 }),
                                   new SimpleBounds(new double[] { -10, 0 },
                                                    new double[] { 20, 30 }));
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testMoreEstimatedParametersSimple
    public void testMoreEstimatedParametersSimple() {
        
        super.testMoreEstimatedParametersSimple();
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testMoreEstimatedParametersUnsorted
    public void testMoreEstimatedParametersUnsorted() {
        
        super.testMoreEstimatedParametersUnsorted();
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testMaxEvaluations
    public void testMaxEvaluations() throws Exception {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);

        GaussNewtonOptimizer optimizer
            = new GaussNewtonOptimizer(new SimpleVectorValueChecker(1e-30, 1e-30));

        optimizer.optimize(new MaxEval(100),
                           circle.getModelFunction(),
                           circle.getModelFunctionJacobian(),
                           new Target(new double[] { 0, 0, 0, 0, 0 }),
                           new Weight(new double[] { 1, 1, 1, 1, 1 }),
                           new InitialGuess(new double[] { 98.680, 47.345 }));
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testCircleFittingBadInit
    public void testCircleFittingBadInit() {
        
        super.testCircleFittingBadInit();
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.GaussNewtonOptimizerTest::testHahn1
    public void testHahn1()
        throws IOException {
        
        super.testHahn1();
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest::testConstraintsUnsupported
    public void testConstraintsUnsupported() {
        createOptimizer().optimize(new MaxEval(100),
                                   new Target(new double[] { 2 }),
                                   new Weight(new double[] { 1 }),
                                   new InitialGuess(new double[] { 1, 2 }),
                                   new SimpleBounds(new double[] { -10, 0 },
                                                    new double[] { 20, 30 }));
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest::testNonInvertible
    public void testNonInvertible() {
        
        LinearProblem problem = new LinearProblem(new double[][] {
                {  1, 2, -3 },
                {  2, 1,  3 },
                { -3, 0, -9 }
        }, new double[] { 1, 1, 1 });

        AbstractLeastSquaresOptimizer optimizer = createOptimizer();
        PointVectorValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getModelFunction(),
                                 problem.getModelFunctionJacobian(),
                                 problem.getTarget(),
                                 new Weight(new double[] { 1, 1, 1 }),
                                 new InitialGuess(new double[] { 0, 0, 0 }));
        Assert.assertTrue(FastMath.sqrt(optimizer.getTargetSize()) * optimizer.getRMS() > 0.6);

        optimizer.computeCovariances(optimum.getPoint(), 1.5e-14);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest::testControlParameters
    public void testControlParameters() {
        CircleVectorial circle = new CircleVectorial();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        checkEstimate(circle.getModelFunction(),
                      circle.getModelFunctionJacobian(),
                      0.1, 10, 1.0e-14, 1.0e-16, 1.0e-10, false);
        checkEstimate(circle.getModelFunction(),
                      circle.getModelFunctionJacobian(),
                      0.1, 10, 1.0e-15, 1.0e-17, 1.0e-10, true);
        checkEstimate(circle.getModelFunction(),
                      circle.getModelFunctionJacobian(),
                      0.1,  5, 1.0e-15, 1.0e-16, 1.0e-10, true);
        circle.addPoint(300, -300);
        checkEstimate(circle.getModelFunction(),
                      circle.getModelFunctionJacobian(),
                      0.1, 20, 1.0e-18, 1.0e-16, 1.0e-10, true);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest::testBevington
    public void testBevington() {
        final double[][] dataPoints = {
            
            { 15, 30, 45, 60, 75, 90, 105, 120, 135, 150,
              165, 180, 195, 210, 225, 240, 255, 270, 285, 300,
              315, 330, 345, 360, 375, 390, 405, 420, 435, 450,
              465, 480, 495, 510, 525, 540, 555, 570, 585, 600,
              615, 630, 645, 660, 675, 690, 705, 720, 735, 750,
              765, 780, 795, 810, 825, 840, 855, 870, 885, },
            
            { 775, 479, 380, 302, 185, 157, 137, 119, 110, 89,
              74, 61, 66, 68, 48, 54, 51, 46, 55, 29,
              28, 37, 49, 26, 35, 29, 31, 24, 25, 35,
              24, 30, 26, 28, 21, 18, 20, 27, 17, 17,
              14, 17, 24, 11, 22, 17, 12, 10, 13, 16,
              9, 9, 14, 21, 17, 13, 12, 18, 10, },
        };

        final BevingtonProblem problem = new BevingtonProblem();

        final int len = dataPoints[0].length;
        final double[] weights = new double[len];
        for (int i = 0; i < len; i++) {
            problem.addPoint(dataPoints[0][i],
                             dataPoints[1][i]);

            weights[i] = 1 / dataPoints[1][i];
        }

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();

        final PointVectorValuePair optimum
            = optimizer.optimize(new MaxEval(100),
                                 problem.getModelFunction(),
                                 problem.getModelFunctionJacobian(),
                                 new Target(dataPoints[1]),
                                 new Weight(weights),
                                 new InitialGuess(new double[] { 10, 900, 80, 27, 225 }));

        final double[] solution = optimum.getPoint();
        final double[] expectedSolution = { 10.4, 958.3, 131.4, 33.9, 205.0 };

        final double[][] covarMatrix = optimizer.computeCovariances(solution, 1e-14);
        final double[][] expectedCovarMatrix = {
            { 3.38, -3.69, 27.98, -2.34, -49.24 },
            { -3.69, 2492.26, 81.89, -69.21, -8.9 },
            { 27.98, 81.89, 468.99, -44.22, -615.44 },
            { -2.34, -69.21, -44.22, 6.39, 53.80 },
            { -49.24, -8.9, -615.44, 53.8, 929.45 }
        };

        final int numParams = expectedSolution.length;

        
        for (int i = 0; i < numParams; i++) {
            final double error = FastMath.sqrt(expectedCovarMatrix[i][i]);
            Assert.assertEquals("Parameter " + i, expectedSolution[i], solution[i], error);
        }

        
        
        for (int i = 0; i < numParams; i++) {
            for (int j = 0; j < numParams; j++) {
                Assert.assertEquals("Covariance matrix [" + i + "][" + j + "]",
                                    expectedCovarMatrix[i][j],
                                    covarMatrix[i][j],
                                    FastMath.abs(0.1 * expectedCovarMatrix[i][j]));
            }
        }
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizerTest::testCircleFitting2
    public void testCircleFitting2() {
        final double xCenter = 123.456;
        final double yCenter = 654.321;
        final double xSigma = 10;
        final double ySigma = 15;
        final double radius = 111.111;
        
        final long seed = 59421061L;
        final RandomCirclePointGenerator factory
            = new RandomCirclePointGenerator(xCenter, yCenter, radius,
                                             xSigma, ySigma,
                                             seed);
        final CircleProblem circle = new CircleProblem(xSigma, ySigma);

        final int numPoints = 10;
        for (Vector2D p : factory.generate(numPoints)) {
            circle.addPoint(p.getX(), p.getY());
        }

        
        final double[] init = { 90, 659, 115 };

        final LevenbergMarquardtOptimizer optimizer
            = new LevenbergMarquardtOptimizer();
        final PointVectorValuePair optimum = optimizer.optimize(new MaxEval(100),
                                                                circle.getModelFunction(),
                                                                circle.getModelFunctionJacobian(),
                                                                new Target(circle.target()),
                                                                new Weight(circle.weight()),
                                                                new InitialGuess(init));

        final double[] paramFound = optimum.getPoint();

        
        final double[] asymptoticStandardErrorFound = optimizer.computeSigma(paramFound, 1e-14);

        
        Assert.assertEquals(xCenter, paramFound[0], asymptoticStandardErrorFound[0]);
        Assert.assertEquals(yCenter, paramFound[1], asymptoticStandardErrorFound[1]);
        Assert.assertEquals(radius, paramFound[2], asymptoticStandardErrorFound[2]);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackLinearFullRank
    public void testMinpackLinearFullRank() {
        minpackTest(new LinearFullRankFunction(10, 5, 1.0,
                                               5.0, 2.23606797749979), false);
        minpackTest(new LinearFullRankFunction(50, 5, 1.0,
                                               8.06225774829855, 6.70820393249937), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackLinearRank1
    public void testMinpackLinearRank1() {
        minpackTest(new LinearRank1Function(10, 5, 1.0,
                                            291.521868819476, 1.4638501094228), false);
        minpackTest(new LinearRank1Function(50, 5, 1.0,
                                            3101.60039334535, 3.48263016573496), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackLinearRank1ZeroColsAndRows
    public void testMinpackLinearRank1ZeroColsAndRows() {
        minpackTest(new LinearRank1ZeroColsAndRowsFunction(10, 5, 1.0), false);
        minpackTest(new LinearRank1ZeroColsAndRowsFunction(50, 5, 1.0), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackRosenbrok
    public void testMinpackRosenbrok() {
        minpackTest(new RosenbrockFunction(new double[] { -1.2, 1.0 },
                                           FastMath.sqrt(24.2)), false);
        minpackTest(new RosenbrockFunction(new double[] { -12.0, 10.0 },
                                           FastMath.sqrt(1795769.0)), false);
        minpackTest(new RosenbrockFunction(new double[] { -120.0, 100.0 },
                                           11.0 * FastMath.sqrt(169000121.0)), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackHelicalValley
    public void testMinpackHelicalValley() {
        minpackTest(new HelicalValleyFunction(new double[] { -1.0, 0.0, 0.0 },
                                              50.0), false);
        minpackTest(new HelicalValleyFunction(new double[] { -10.0, 0.0, 0.0 },
                                              102.95630140987), false);
        minpackTest(new HelicalValleyFunction(new double[] { -100.0, 0.0, 0.0},
                                              991.261822123701), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackPowellSingular
    public void testMinpackPowellSingular() {
        minpackTest(new PowellSingularFunction(new double[] { 3.0, -1.0, 0.0, 1.0 },
                                               14.6628782986152), false);
        minpackTest(new PowellSingularFunction(new double[] { 30.0, -10.0, 0.0, 10.0 },
                                               1270.9838708654), false);
        minpackTest(new PowellSingularFunction(new double[] { 300.0, -100.0, 0.0, 100.0 },
                                               126887.903284750), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackFreudensteinRoth
    public void testMinpackFreudensteinRoth() {
        minpackTest(new FreudensteinRothFunction(new double[] { 0.5, -2.0 },
                                                 20.0124960961895, 6.99887517584575,
                                                 new double[] {
                                                     11.4124844654993,
                                                     -0.896827913731509
                                                 }), false);
        minpackTest(new FreudensteinRothFunction(new double[] { 5.0, -20.0 },
                                                 12432.833948863, 6.9988751744895,
                                                 new double[] {
                                                     11.41300466147456,
                                                     -0.896796038685959
                                                 }), false);
        minpackTest(new FreudensteinRothFunction(new double[] { 50.0, -200.0 },
                                                 11426454.595762, 6.99887517242903,
                                                 new double[] {
                                                     11.412781785788564,
                                                     -0.8968051074920405
                                                 }), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackBard
    public void testMinpackBard() {
        minpackTest(new BardFunction(1.0, 6.45613629515967, 0.0906359603390466,
                                     new double[] {
                                         0.0824105765758334,
                                         1.1330366534715,
                                         2.34369463894115
                                     }), false);
        minpackTest(new BardFunction(10.0, 36.1418531596785, 4.17476870138539,
                                     new double[] {
                                         0.840666673818329,
                                         -158848033.259565,
                                         -164378671.653535
                                     }), false);
        minpackTest(new BardFunction(100.0, 384.114678637399, 4.17476870135969,
                                     new double[] {
                                         0.840666673867645,
                                         -158946167.205518,
                                         -164464906.857771
                                     }), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackKowalikOsborne
    public void testMinpackKowalikOsborne() {
        minpackTest(new KowalikOsborneFunction(new double[] { 0.25, 0.39, 0.415, 0.39 },
                                               0.0728915102882945,
                                               0.017535837721129,
                                               new double[] {
                                                   0.192807810476249,
                                                   0.191262653354071,
                                                   0.123052801046931,
                                                   0.136053221150517
                                               }), false);
        minpackTest(new KowalikOsborneFunction(new double[] { 2.5, 3.9, 4.15, 3.9 },
                                               2.97937007555202,
                                               0.032052192917937,
                                               new double[] {
                                                   728675.473768287,
                                                   -14.0758803129393,
                                                   -32977797.7841797,
                                                   -20571594.1977912
                                               }), false);
        minpackTest(new KowalikOsborneFunction(new double[] { 25.0, 39.0, 41.5, 39.0 },
                                               29.9590617016037,
                                               0.0175364017658228,
                                               new double[] {
                                                   0.192948328597594,
                                                   0.188053165007911,
                                                   0.122430604321144,
                                                   0.134575665392506
                                               }), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackMeyer
    public void testMinpackMeyer() {
        minpackTest(new MeyerFunction(new double[] { 0.02, 4000.0, 250.0 },
                                      41153.4665543031, 9.37794514651874,
                                      new double[] {
                                          0.00560963647102661,
                                          6181.34634628659,
                                          345.223634624144
                                      }), false);
        minpackTest(new MeyerFunction(new double[] { 0.2, 40000.0, 2500.0 },
                                      4168216.89130846, 792.917871779501,
                                      new double[] {
                                          1.42367074157994e-11,
                                          33695.7133432541,
                                          901.268527953801
                                      }), true);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackWatson
    public void testMinpackWatson() {
        minpackTest(new WatsonFunction(6, 0.0,
                                       5.47722557505166, 0.0478295939097601,
                                       new double[] {
                                           -0.0157249615083782, 1.01243488232965,
                                           -0.232991722387673,  1.26043101102818,
                                           -1.51373031394421,   0.99299727291842
                                       }), false);
        minpackTest(new WatsonFunction(6, 10.0,
                                       6433.12578950026, 0.0478295939096951,
                                       new double[] {
                                           -0.0157251901386677, 1.01243485860105,
                                           -0.232991545843829,  1.26042932089163,
                                           -1.51372776706575,   0.99299573426328
                                       }), false);
        minpackTest(new WatsonFunction(6, 100.0,
                                       674256.040605213, 0.047829593911544,
                                       new double[] {
                                           -0.0157247019712586, 1.01243490925658,
                                           -0.232991922761641,  1.26043292929555,
                                           -1.51373320452707,   0.99299901922322
                                       }), false);
        minpackTest(new WatsonFunction(9, 0.0,
                                       5.47722557505166, 0.00118311459212420,
                                       new double[] {
                                           -0.153070644166722e-4, 0.999789703934597,
                                           0.0147639634910978,   0.146342330145992,
                                           1.00082109454817,    -2.61773112070507,
                                           4.10440313943354,    -3.14361226236241,
                                           1.05262640378759
                                       }), false);
        minpackTest(new WatsonFunction(9, 10.0,
                                       12088.127069307, 0.00118311459212513,
                                       new double[] {
                                           -0.153071334849279e-4, 0.999789703941234,
                                           0.0147639629786217,   0.146342334818836,
                                           1.00082107321386,    -2.61773107084722,
                                           4.10440307655564,    -3.14361222178686,
                                           1.05262639322589
                                       }), false);
        minpackTest(new WatsonFunction(9, 100.0,
                                       1269109.29043834, 0.00118311459212384,
                                       new double[] {
                                           -0.153069523352176e-4, 0.999789703958371,
                                           0.0147639625185392,   0.146342341096326,
                                           1.00082104729164,    -2.61773101573645,
                                           4.10440301427286,    -3.14361218602503,
                                           1.05262638516774
                                       }), false);
        minpackTest(new WatsonFunction(12, 0.0,
                                       5.47722557505166, 0.217310402535861e-4,
                                       new double[] {
                                           -0.660266001396382e-8, 1.00000164411833,
                                           -0.000563932146980154, 0.347820540050756,
                                           -0.156731500244233,    1.05281515825593,
                                           -3.24727109519451,     7.2884347837505,
                                           -10.271848098614,       9.07411353715783,
                                           -4.54137541918194,     1.01201187975044
                                       }), false);
        minpackTest(new WatsonFunction(12, 10.0,
                                       19220.7589790951, 0.217310402518509e-4,
                                       new double[] {
                                           -0.663710223017410e-8, 1.00000164411787,
                                           -0.000563932208347327, 0.347820540486998,
                                           -0.156731503955652,    1.05281517654573,
                                           -3.2472711515214,      7.28843489430665,
                                           -10.2718482369638,      9.07411364383733,
                                           -4.54137546533666,     1.01201188830857
                                       }), false);
        minpackTest(new WatsonFunction(12, 100.0,
                                       2018918.04462367, 0.217310402539845e-4,
                                       new double[] {
                                           -0.663806046485249e-8, 1.00000164411786,
                                           -0.000563932210324959, 0.347820540503588,
                                           -0.156731504091375,    1.05281517718031,
                                           -3.24727115337025,     7.28843489775302,
                                           -10.2718482410813,      9.07411364688464,
                                           -4.54137546660822,     1.0120118885369
                                       }), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackBox3Dimensional
    public void testMinpackBox3Dimensional() {
        minpackTest(new Box3DimensionalFunction(10, new double[] { 0.0, 10.0, 20.0 },
                                                32.1115837449572), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackJennrichSampson
    public void testMinpackJennrichSampson() {
        minpackTest(new JennrichSampsonFunction(10, new double[] { 0.3, 0.4 },
                                                64.5856498144943, 11.1517793413499,
                                                new double[] {

                                                    0.2578199266368004, 0.25782997676455244
                                                }), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackBrownDennis
    public void testMinpackBrownDennis() {
        minpackTest(new BrownDennisFunction(20,
                                            new double[] { 25.0, 5.0, -5.0, -1.0 },
                                            2815.43839161816, 292.954288244866,
                                            new double[] {
                                                -11.59125141003, 13.2024883984741,
                                                -0.403574643314272, 0.236736269844604
                                            }), false);
        minpackTest(new BrownDennisFunction(20,
                                            new double[] { 250.0, 50.0, -50.0, -10.0 },
                                            555073.354173069, 292.954270581415,
                                            new double[] {
                                                -11.5959274272203, 13.2041866926242,
                                                -0.403417362841545, 0.236771143410386
                                            }), false);
        minpackTest(new BrownDennisFunction(20,
                                            new double[] { 2500.0, 500.0, -500.0, -100.0 },
                                            61211252.2338581, 292.954306151134,
                                            new double[] {
                                                -11.5902596937374, 13.2020628854665,
                                                -0.403688070279258, 0.236665033746463
                                            }), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackChebyquad
    public void testMinpackChebyquad() {
        minpackTest(new ChebyquadFunction(1, 8, 1.0,
                                          1.88623796907732, 1.88623796907732,
                                          new double[] { 0.5 }), false);
        minpackTest(new ChebyquadFunction(1, 8, 10.0,
                                          5383344372.34005, 1.88424820499951,
                                          new double[] { 0.9817314924684 }), false);
        minpackTest(new ChebyquadFunction(1, 8, 100.0,
                                          0.118088726698392e19, 1.88424820499347,
                                          new double[] { 0.9817314852934 }), false);
        minpackTest(new ChebyquadFunction(8, 8, 1.0,
                                          0.196513862833975, 0.0593032355046727,
                                          new double[] {
                                              0.0431536648587336, 0.193091637843267,
                                              0.266328593812698,  0.499999334628884,
                                              0.500000665371116,  0.733671406187302,
                                              0.806908362156733,  0.956846335141266
                                          }), false);
        minpackTest(new ChebyquadFunction(9, 9, 1.0,
                                          0.16994993465202, 0.0,
                                          new double[] {
                                              0.0442053461357828, 0.199490672309881,
                                              0.23561910847106,   0.416046907892598,
                                              0.5,                0.583953092107402,
                                              0.764380891528940,  0.800509327690119,
                                              0.955794653864217
                                          }), false);
        minpackTest(new ChebyquadFunction(10, 10, 1.0,
                                          0.183747831178711, 0.0806471004038253,
                                          new double[] {
                                              0.0596202671753563, 0.166708783805937,
                                              0.239171018813509,  0.398885290346268,
                                              0.398883667870681,  0.601116332129320,
                                              0.60111470965373,   0.760828981186491,
                                              0.833291216194063,  0.940379732824644
                                          }), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackBrownAlmostLinear
    public void testMinpackBrownAlmostLinear() {
        minpackTest(new BrownAlmostLinearFunction(10, 0.5,
                                                  16.5302162063499, 0.0,
                                                  new double[] {
                                                      0.979430303349862, 0.979430303349862,
                                                      0.979430303349862, 0.979430303349862,
                                                      0.979430303349862, 0.979430303349862,
                                                      0.979430303349862, 0.979430303349862,
                                                      0.979430303349862, 1.20569696650138
                                                  }), false);
        minpackTest(new BrownAlmostLinearFunction(10, 5.0,
                                                  9765624.00089211, 0.0,
                                                  new double[] {
                                                      0.979430303349865, 0.979430303349865,
                                                      0.979430303349865, 0.979430303349865,
                                                      0.979430303349865, 0.979430303349865,
                                                      0.979430303349865, 0.979430303349865,
                                                      0.979430303349865, 1.20569696650135
                                                  }), false);
        minpackTest(new BrownAlmostLinearFunction(10, 50.0,
                                                  0.9765625e17, 0.0,
                                                  new double[] {
                                                      1.0, 1.0, 1.0, 1.0, 1.0,
                                                      1.0, 1.0, 1.0, 1.0, 1.0
                                                  }), false);
        minpackTest(new BrownAlmostLinearFunction(30, 0.5,
                                                  83.476044467848, 0.0,
                                                  new double[] {
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 0.997754216442807,
                                                      0.997754216442807, 1.06737350671578
                                                  }), false);
        minpackTest(new BrownAlmostLinearFunction(40, 0.5,
                                                  128.026364472323, 0.0,
                                                  new double[] {
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      1.00000000000002, 1.00000000000002,
                                                      0.999999999999121
                                                  }), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackOsborne1
    public void testMinpackOsborne1() {
        minpackTest(new Osborne1Function(new double[] { 0.5, 1.5, -1.0, 0.01, 0.02, },
                                         0.937564021037838, 0.00739249260904843,
                                         new double[] {
                                             0.375410049244025, 1.93584654543108,
                                             -1.46468676748716, 0.0128675339110439,
                                             0.0221227011813076
                                         }), false);
    }

// org.apache.commons.math3.optim.nonlinear.vector.jacobian.MinpackTest::testMinpackOsborne2
    public void testMinpackOsborne2() {
        minpackTest(new Osborne2Function(new double[] {
                    1.3, 0.65, 0.65, 0.7, 0.6,
                    3.0, 5.0, 7.0, 2.0, 4.5, 5.5
                },
                1.44686540984712, 0.20034404483314,
                new double[] {
                    1.30997663810096,  0.43155248076,
                    0.633661261602859, 0.599428560991695,
                    0.754179768272449, 0.904300082378518,
                    1.36579949521007, 4.82373199748107,
                    2.39868475104871, 4.56887554791452,
                    5.67534206273052
                }), false);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testSinMin
    public void testSinMin() {
        UnivariateFunction f = new Sin();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(3 * Math.PI / 2, optimizer.optimize(new MaxEval(200),
                                                                new UnivariateObjectiveFunction(f),
                                                                GoalType.MINIMIZE,
                                                                new SearchInterval(4, 5)).getPoint(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        Assert.assertEquals(3 * Math.PI / 2, optimizer.optimize(new MaxEval(200),
                                                                new UnivariateObjectiveFunction(f),
                                                                GoalType.MINIMIZE,
                                                                new SearchInterval(1, 5)).getPoint(), 1e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 100);
        Assert.assertTrue(optimizer.getEvaluations() >= 15);
        try {
            optimizer.optimize(new MaxEval(10),
                               new UnivariateObjectiveFunction(f),
                               GoalType.MINIMIZE,
                               new SearchInterval(4, 5));
            Assert.fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException fee) {
            
        }
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testSinMinWithValueChecker
    public void testSinMinWithValueChecker() {
        final UnivariateFunction f = new Sin();
        final ConvergenceChecker<UnivariatePointValuePair> checker = new SimpleUnivariateValueChecker(1e-5, 1e-14);
        
        
        
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14, checker);
        final UnivariatePointValuePair result = optimizer.optimize(new MaxEval(200),
                                                                   new UnivariateObjectiveFunction(f),
                                                                   GoalType.MINIMIZE,
                                                                   new SearchInterval(4, 5));
        Assert.assertEquals(3 * Math.PI / 2, result.getPoint(), 1e-3);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testBoundaries
    public void testBoundaries() {
        final double lower = -1.0;
        final double upper = +1.0;
        UnivariateFunction f = new UnivariateFunction() {            
            public double value(double x) {
                if (x < lower) {
                    throw new NumberIsTooSmallException(x, lower, true);
                } else if (x > upper) {
                    throw new NumberIsTooLargeException(x, upper, true);
                } else {
                    return x;
                }
            }
        };
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(lower,
                            optimizer.optimize(new MaxEval(100),
                                               new UnivariateObjectiveFunction(f),
                                               GoalType.MINIMIZE,
                                               new SearchInterval(lower, upper)).getPoint(),
                            1.0e-8);
        Assert.assertEquals(upper,
                            optimizer.optimize(new MaxEval(100),
                                               new UnivariateObjectiveFunction(f),
                                               GoalType.MAXIMIZE,
                                               new SearchInterval(lower, upper)).getPoint(),
                            1.0e-8);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testQuinticMin
    public void testQuinticMin() {
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);
        Assert.assertEquals(-0.27195613, optimizer.optimize(new MaxEval(200),
                                                            new UnivariateObjectiveFunction(f),
                                                            GoalType.MINIMIZE,
                                                            new SearchInterval(-0.3, -0.2)).getPoint(), 1.0e-8);
        Assert.assertEquals( 0.82221643, optimizer.optimize(new MaxEval(200),
                                                            new UnivariateObjectiveFunction(f),
                                                            GoalType.MINIMIZE,
                                                            new SearchInterval(0.3,  0.9)).getPoint(), 1.0e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);

        
        Assert.assertEquals(-0.27195613, optimizer.optimize(new MaxEval(200),
                                                            new UnivariateObjectiveFunction(f),
                                                            GoalType.MINIMIZE,
                                                            new SearchInterval(-1.0, 0.2)).getPoint(), 1.0e-8);
        Assert.assertTrue(optimizer.getEvaluations() <= 50);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testQuinticMinStatistics
    public void testQuinticMinStatistics() {
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-11, 1e-14);

        final DescriptiveStatistics[] stat = new DescriptiveStatistics[2];
        for (int i = 0; i < stat.length; i++) {
            stat[i] = new DescriptiveStatistics();
        }

        final double min = -0.75;
        final double max = 0.25;
        final int nSamples = 200;
        final double delta = (max - min) / nSamples;
        for (int i = 0; i < nSamples; i++) {
            final double start = min + i * delta;
            stat[0].addValue(optimizer.optimize(new MaxEval(40),
                                                new UnivariateObjectiveFunction(f),
                                                GoalType.MINIMIZE,
                                                new SearchInterval(min, max, start)).getPoint());
            stat[1].addValue(optimizer.getEvaluations());
        }

        final double meanOptValue = stat[0].getMean();
        final double medianEval = stat[1].getPercentile(50);
        Assert.assertTrue(meanOptValue > -0.2719561281);
        Assert.assertTrue(meanOptValue < -0.2719561280);
        Assert.assertEquals(23, (int) medianEval);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testQuinticMax
    public void testQuinticMax() {
        
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-12, 1e-14);
        Assert.assertEquals(0.27195613, optimizer.optimize(new MaxEval(100),
                                                           new UnivariateObjectiveFunction(f),
                                                           GoalType.MAXIMIZE,
                                                           new SearchInterval(0.2, 0.3)).getPoint(), 1e-8);
        try {
            optimizer.optimize(new MaxEval(5),
                               new UnivariateObjectiveFunction(f),
                               GoalType.MAXIMIZE,
                               new SearchInterval(0.2, 0.3));
            Assert.fail("an exception should have been thrown");
        } catch (TooManyEvaluationsException miee) {
            
        }
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testMinEndpoints
    public void testMinEndpoints() {
        UnivariateFunction f = new Sin();
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-8, 1e-14);

        
        double result = optimizer.optimize(new MaxEval(50),
                                           new UnivariateObjectiveFunction(f),
                                           GoalType.MINIMIZE,
                                           new SearchInterval(3 * Math.PI / 2, 5)).getPoint();
        Assert.assertEquals(3 * Math.PI / 2, result, 1e-6);

        result = optimizer.optimize(new MaxEval(50),
                                    new UnivariateObjectiveFunction(f),
                                    GoalType.MINIMIZE,
                                    new SearchInterval(4, 3 * Math.PI / 2)).getPoint();
        Assert.assertEquals(3 * Math.PI / 2, result, 1e-6);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testMath832
    public void testMath832() {
        final UnivariateFunction f = new UnivariateFunction() {
                public double value(double x) {
                    final double sqrtX = FastMath.sqrt(x);
                    final double a = 1e2 * sqrtX;
                    final double b = 1e6 / x;
                    final double c = 1e4 / sqrtX;

                    return a + b + c;
                }
            };

        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-8);
        final double result = optimizer.optimize(new MaxEval(1483),
                                                 new UnivariateObjectiveFunction(f),
                                                 GoalType.MINIMIZE,
                                                 new SearchInterval(Double.MIN_VALUE,
                                                                    Double.MAX_VALUE)).getPoint();

        Assert.assertEquals(804.9355825, result, 1e-6);
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testKeepInitIfBest
    public void testKeepInitIfBest() {
        final double minSin = 3 * Math.PI / 2;
        final double offset = 1e-8;
        final double delta = 1e-7;
        final UnivariateFunction f1 = new Sin();
        final UnivariateFunction f2 = new StepFunction(new double[] { minSin, minSin + offset, minSin + 2 * offset},
                                                       new double[] { 0, -1, 0 });
        final UnivariateFunction f = FunctionUtils.add(f1, f2);
        
        
        final double relTol = 1e-8;
        final UnivariateOptimizer optimizer = new BrentOptimizer(relTol, 1e-100);
        final double init = minSin + 1.5 * offset;
        final UnivariatePointValuePair result
            = optimizer.optimize(new MaxEval(200),
                                 new UnivariateObjectiveFunction(f),
                                 GoalType.MINIMIZE,
                                 new SearchInterval(minSin - 6.789 * delta,
                                                    minSin + 9.876 * delta,
                                                    init));
        final int numEval = optimizer.getEvaluations();

        final double sol = result.getPoint();
        final double expected = init;

        Assert.assertTrue("Best point not reported", f.value(sol) <= f.value(expected));
    }

// org.apache.commons.math3.optim.univariate.BrentOptimizerTest::testMath855
    public void testMath855() {
        final double minSin = 3 * Math.PI / 2;
        final double offset = 1e-8;
        final double delta = 1e-7;
        final UnivariateFunction f1 = new Sin();
        final UnivariateFunction f2 = new StepFunction(new double[] { minSin, minSin + offset, minSin + 5 * offset },
                                                       new double[] { 0, -1, 0 });
        final UnivariateFunction f = FunctionUtils.add(f1, f2);
        final UnivariateOptimizer optimizer = new BrentOptimizer(1e-8, 1e-100);
        final UnivariatePointValuePair result
            = optimizer.optimize(new MaxEval(200),
                                 new UnivariateObjectiveFunction(f),
                                 GoalType.MINIMIZE,
                                 new SearchInterval(minSin - 6.789 * delta,
                                                    minSin + 9.876 * delta));
        final int numEval = optimizer.getEvaluations();

        final double sol = result.getPoint();
        final double expected = 4.712389027602411;

        
        
        

        Assert.assertTrue("Best point not reported", f.value(sol) <= f.value(expected));
    }

// org.apache.commons.math3.optim.univariate.MultiStartUnivariateOptimizerTest::testMissingMaxEval
    public void testMissingMaxEval() {
        UnivariateOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(44428400075l);
        MultiStartUnivariateOptimizer optimizer = new MultiStartUnivariateOptimizer(underlying, 10, g);
        optimizer.optimize(new UnivariateObjectiveFunction(new Sin()),
                           GoalType.MINIMIZE,
                           new SearchInterval(-1, 1));
    }

// org.apache.commons.math3.optim.univariate.MultiStartUnivariateOptimizerTest::testMissingSearchInterval
    public void testMissingSearchInterval() {
        UnivariateOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(44428400075l);
        MultiStartUnivariateOptimizer optimizer = new MultiStartUnivariateOptimizer(underlying, 10, g);
        optimizer.optimize(new MaxEval(300),
                           new UnivariateObjectiveFunction(new Sin()),
                           GoalType.MINIMIZE);
    }

// org.apache.commons.math3.optim.univariate.MultiStartUnivariateOptimizerTest::testSinMin
    public void testSinMin() {
        UnivariateFunction f = new Sin();
        UnivariateOptimizer underlying = new BrentOptimizer(1e-10, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(44428400075l);
        MultiStartUnivariateOptimizer optimizer = new MultiStartUnivariateOptimizer(underlying, 10, g);
        optimizer.optimize(new MaxEval(300),
                           new UnivariateObjectiveFunction(f),
                           GoalType.MINIMIZE,
                           new SearchInterval(-100.0, 100.0));
        UnivariatePointValuePair[] optima = optimizer.getOptima();
        for (int i = 1; i < optima.length; ++i) {
            double d = (optima[i].getPoint() - optima[i-1].getPoint()) / (2 * FastMath.PI);
            Assert.assertTrue(FastMath.abs(d - FastMath.rint(d)) < 1.0e-8);
            Assert.assertEquals(-1.0, f.value(optima[i].getPoint()), 1.0e-10);
            Assert.assertEquals(f.value(optima[i].getPoint()), optima[i].getValue(), 1.0e-10);
        }
        Assert.assertTrue(optimizer.getEvaluations() > 200);
        Assert.assertTrue(optimizer.getEvaluations() < 300);
    }

// org.apache.commons.math3.optim.univariate.MultiStartUnivariateOptimizerTest::testQuinticMin
    public void testQuinticMin() {
        
        
        UnivariateFunction f = new QuinticFunction();
        UnivariateOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(4312000053L);
        MultiStartUnivariateOptimizer optimizer = new MultiStartUnivariateOptimizer(underlying, 5, g);

        UnivariatePointValuePair optimum
            = optimizer.optimize(new MaxEval(300),
                                 new UnivariateObjectiveFunction(f),
                                 GoalType.MINIMIZE,
                                 new SearchInterval(-0.3, -0.2));
        Assert.assertEquals(-0.27195613, optimum.getPoint(), 1e-9);
        Assert.assertEquals(-0.0443342695, optimum.getValue(), 1e-9);

        UnivariatePointValuePair[] optima = optimizer.getOptima();
        for (int i = 0; i < optima.length; ++i) {
            Assert.assertEquals(f.value(optima[i].getPoint()), optima[i].getValue(), 1e-9);
        }
        Assert.assertTrue(optimizer.getEvaluations() >= 50);
        Assert.assertTrue(optimizer.getEvaluations() <= 100);
    }

// org.apache.commons.math3.optim.univariate.MultiStartUnivariateOptimizerTest::testBadFunction
    public void testBadFunction() {
        UnivariateFunction f = new UnivariateFunction() {
                public double value(double x) {
                    if (x < 0) {
                        throw new LocalException();
                    }
                    return 0;
                }
            };
        UnivariateOptimizer underlying = new BrentOptimizer(1e-9, 1e-14);
        JDKRandomGenerator g = new JDKRandomGenerator();
        g.setSeed(4312000053L);
        MultiStartUnivariateOptimizer optimizer = new MultiStartUnivariateOptimizer(underlying, 5, g);
 
        try {
            optimizer.optimize(new MaxEval(300),
                               new UnivariateObjectiveFunction(f),
                               GoalType.MINIMIZE,
                               new SearchInterval(-0.3, -0.2));
            Assert.fail();
        } catch (LocalException e) {
            
        }

        
        Assert.assertTrue(optimizer.getOptima()[0] == null);
    }
