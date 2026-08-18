public RealMatrix getV()
        throws InvalidMatrixException {

        if (cachedV == null) {

            final int p = singularValues.length;
            if (m >= n) {
                // the tridiagonal matrix is Bt.B, where B is upper bidiagonal
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, n - 1, 0, p - 1);
                final double[][] eData = e.getData();
                // Reverse columns
                int rows = eData.length;
                int cols = eData[0].length;
                double[][] revEData = new double[rows][cols];
                for (int j = 0; j < cols; ++j) {
                    for (int i = 0; i < rows; ++i) {
                        revEData[i][j] = eData[i][cols - 1 - j];
                    }
                }
                cachedV = transformer.getV().multiply(MatrixUtils.createRealMatrix(revEData));
            } else {
                // the tridiagonal matrix is B.Bt, where B is lower bidiagonal
                // compute W = Bt.E.S^(-1) where E is the eigenvectors matrix
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, p - 1, 0, p - 1);
                final double[][] eData = e.getData();
                // Reverse columns
                int rows = eData.length;
                int cols = eData[0].length;
                double[][] revEData = new double[rows][cols];
                for (int j = 0; j < cols; ++j) {
                    for (int i = 0; i < rows; ++i) {
                        revEData[i][j] = eData[i][cols - 1 - j];
                    }
                }
                final double[][] wData = new double[n][p];
                double[] ei1 = revEData[0];
                for (int i = 0; i < p - 1; ++i) {
                    final double mi = mainBidiagonal[i];
                    final double[] ei0 = ei1;
                    final double[] wi  = wData[i];
                        ei1 = revEData[i + 1];
                        final double si = secondaryBidiagonal[i];
                        for (int j = 0; j < p; ++j) {
                            wi[j] = (mi * ei0[j] + si * ei1[j]) / singularValues[j];
                        }
                }
                        for (int j = 0; j < p; ++j) {
                            wData[p - 1][j] = ei1[j] * mainBidiagonal[p - 1] / singularValues[j];
                        }
                for (int i = p; i < n; ++i) {
                    wData[i] = new double[p];
                }
                cachedV =
                    transformer.getV().multiply(MatrixUtils.createRealMatrix(wData));
            }

        }

        // return the cached matrix
        return cachedV;

    }