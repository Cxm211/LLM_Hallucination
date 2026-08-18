public PointVectorValuePair doOptimize() {
    checkParameters();

    final ConvergenceChecker<PointVectorValuePair> checker
        = getConvergenceChecker();

    if (checker == null) {
        throw new NullArgumentException();
    }

    final double[] targetValues = getTarget();
    final int nR = targetValues.length;

    final RealMatrix weightMatrix = getWeight();
    final double[] residualsWeights = new double[nR];
    for (int i = 0; i < nR; i++) {
        residualsWeights[i] = weightMatrix.getEntry(i, i);
    }

    final double[] currentPoint = getStartPoint();
    final int nC = currentPoint.length;

    PointVectorValuePair current = null;
    int iter = 0;
    for (boolean converged = false; !converged;) {
        ++iter;

        PointVectorValuePair previous = current;
        final double[] currentObjective = computeObjectiveValue(currentPoint);
        final double[] currentResiduals = computeResiduals(currentObjective);
        final RealMatrix weightedJacobian = computeWeightedJacobian(currentPoint);
        current = new PointVectorValuePair(currentPoint, currentObjective);

        final double[]   b = new double[nC];
        final double[][] a = new double[nC][nC];
        for (int i = 0; i < nR; ++i) {

            final double[] grad   = weightedJacobian.getRow(i);
            final double weight   = residualsWeights[i];
            final double residual = currentResiduals[i];

            final double wr = weight * residual;
            for (int j = 0; j < nC; ++j) {
                b[j] += wr * grad[j];
            }

            for (int k = 0; k < nC; ++k) {
                double[] ak = a[k];
                double wgk = weight * grad[k];
                for (int l = 0; l < nC; ++l) {
                    ak[l] += wgk * grad[l];
                }
            }
        }

        try {
            RealMatrix mA = new BlockRealMatrix(a);
            DecompositionSolver solver = useLU ?
                    new LUDecomposition(mA).getSolver() :
                    new QRDecomposition(mA).getSolver();
            final double[] dX = solver.solve(new ArrayRealVector(b, false)).toArray();
            for (int i = 0; i < nC; ++i) {
                currentPoint[i] += dX[i];
            }
        } catch (SingularMatrixException e) {
            throw new ConvergenceException(LocalizedFormats.UNABLE_TO_SOLVE_SINGULAR_PROBLEM);
        }

        if (previous != null) {
            converged = checker.converged(iter, previous, current);
            if (converged) {
                setCost(computeCost(currentResiduals));
                return current;
            }
        }
    }
    throw new MathInternalError();
}