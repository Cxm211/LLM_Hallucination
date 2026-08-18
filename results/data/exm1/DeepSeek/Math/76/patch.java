public RealMatrix getU()
        throws InvalidMatrixException {

        if (cachedU == null) {

            final int p = singularValues.length;
            if (m >= n) {
                // the tridiagonal matrix is Bt.B, where B is upper bidiagonal
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, p - 1, 0, p - 1);
                final double[][] eData = e.getData();
                // Reverse columns to match descending singular value order
                int rows = eData.length;
                int cols = eData[0].length;
                double[][] revEData = new double[rows][cols];
                for (int j = 0; j < cols; ++j) {
                    for (int i = 0; i < rows; ++i) {
                        revEData[i][j] = eData[i][cols - 1 - j];
                    }
                }
                final double[][] wData = new double[m][p];
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

                for (int i = p; i < m; ++i) {
                    wData[i] = new double[p];
                }
                cachedU =
                    transformer.getU().multiply(MatrixUtils.createRealMatrix(wData));
            } else {
                // the tridiagonal matrix is B.Bt, where B is lower bidiagonal
                final RealMatrix e =
                    eigenDecomposition.getV().getSubMatrix(0, m - 1, 0, p - 1);
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
                cachedU = transformer.getU().multiply(MatrixUtils.createRealMatrix(revEData));
            }

        }

        // return the cached matrix
        return cachedU;

    }