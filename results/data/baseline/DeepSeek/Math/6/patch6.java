protected PointVectorValuePair doOptimize() {
    checkParameters();

    final int nR = getTarget().length;
    final double[] currentPoint = getStartPoint();
    final int nC = currentPoint.length;

    solvedCols  = FastMath.min(nR, nC);
    diagR       = new double[nC];
    jacNorm     = new double[nC];
    beta        = new double[nC];
    permutation = new int[nC];
    lmDir       = new double[nC];

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

    double[] currentObjective = computeObjectiveValue(currentPoint);
    double[] currentResiduals = computeResiduals(currentObjective);
    PointVectorValuePair current = new PointVectorValuePair(currentPoint, currentObjective);
    double currentCost = computeCost(currentResiduals);

    lmPar = 0;
    boolean firstIteration = true;
    int iter = 0;
    final ConvergenceChecker<PointVectorValuePair> checker = getConvergenceChecker();
    while (true) {
        ++iter;
        final PointVectorValuePair previous = current;

        weightedJacobian = computeWeightedJacobian(currentPoint);
        qrDecomposition(weightedJacobian);
        final double[][] jacobian = weightedJacobian.getData();

        weightedResidual = weightMatrixSqrt.operate(currentResiduals);
        for (int i = 0; i < nR; i++) {
            qtf[i] = weightedResidual[i];
        }

        qTy(qtf);

        for (int k = 0; k < solvedCols; ++k) {
            int pk = permutation[k];
            jacobian[k][pk] = diagR[pk];
        }

        if (firstIteration) {
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

            delta = (xNorm == 0) ? initialStepBoundFactor : (initialStepBoundFactor * xNorm);
        }

        double maxCosine = 0;
        if (currentCost != 0) {
            for (int j = 0; j < solvedCols; ++j) {
                int    pj = permutation[j];
                double s  = jacNorm[pj];
                if (s != 0) {
                    double sum = 0;
                    for (int i = 0; i <= j; ++i) {
                        sum += jacobian[i][pj] * qtf[i];
                    }
                    maxCosine = FastMath.max(maxCosine, FastMath.abs(sum) / (s * currentCost));
                }
            }
        }
        if (maxCosine <= orthoTolerance) {
            setCost(currentCost);
            return current;
        }

        for (int j = 0; j < nC; ++j) {
            diag[j] = FastMath.max(diag[j], jacNorm[j]);
        }

        for (double ratio = 0; ratio < 1.0e-4;) {

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

            determineLMParameter(qtf, delta, diag, work1, work2, work3);

            double lmNorm = 0;
            for (int j = 0; j < solvedCols; ++j) {
                int pj = permutation[j];
                lmDir[pj] = -lmDir[pj];
                currentPoint[pj] = oldX[pj] + lmDir[pj];
                double s = diag[pj] * lmDir[pj];
                lmNorm  += s * s;
            }
            lmNorm = FastMath.sqrt(lmNorm);
            if (firstIteration) {
                delta = FastMath.min(delta, lmNorm);
            }

            currentObjective = computeObjectiveValue(currentPoint);
            currentResiduals = computeResiduals(currentObjective);
            current = new PointVectorValuePair(currentPoint, currentObjective);
            currentCost = computeCost(currentResiduals);

            double actRed = -1.0;
            if (0.1 * currentCost < previousCost) {
                double r = currentCost / previousCost;
                actRed = 1.0 - r * r;
            }

            for (int j = 0; j < solvedCols; ++j) {
                int pj = permutation[j];
                double dirJ = lmDir[pj];
                work1[j] = 0;
                for (int i = 0; i <= j; ++i) {
                    work1[i] += jacobian[i][pj] * dirJ;
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

            ratio = (preRed == 0) ? 0 : (actRed / preRed);

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

            if (ratio >= 1.0e-4) {
                firstIteration = false;
                xNorm = 0;
                for (int k = 0; k < nC; ++k) {
                    double xK = diag[k] * currentPoint[k];
                    xNorm += xK * xK;
                }
                xNorm = FastMath.sqrt(xNorm);

                if (checker != null) {
                    if (checker.converged(iter, previous, current)) {
                        setCost(currentCost);
                        return current;
                    }
                }
            } else {
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
                current = new PointVectorValuePair(currentPoint, currentObjective);
            }

            if ((FastMath.abs(actRed) <= costRelativeTolerance &&
                 preRed <= costRelativeTolerance &&
                 ratio <= 2.0) ||
                delta <= parRelativeTolerance * xNorm) {
                setCost(currentCost);
                return current;
            }

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