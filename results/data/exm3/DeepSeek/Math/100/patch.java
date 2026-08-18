    public double[][] getCovariances(EstimationProblem problem)
      throws EstimationException {
 
        // set up the jacobian
        updateJacobian();

        // get unbound parameters
        EstimatedParameter[] unbound = problem.getUnboundParameters();
        int n = unbound.length;
        EstimatedParameter[] allParams = problem.getAllParameters();
        int allCols = allParams.length;
        int rows = problem.getMeasurements().length;

        // create mapping from unbound index to allParams index
        int[] indices = new int[n];
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < allCols; ++j) {
                if (unbound[i] == allParams[j]) {
                    indices[i] = j;
                    break;
                }
            }
        }

        // compute transpose(J).J for unbound parameters, avoiding building big intermediate matrices
        double[][] jTj = new double[n][n];
        for (int i = 0; i < n; ++i) {
            for (int j = i; j < n; ++j) {
                double sum = 0;
                for (int k = 0; k < allCols * rows; k += allCols) {
                    sum += jacobian[k + indices[i]] * jacobian[k + indices[j]];
                }
                jTj[i][j] = sum;
                jTj[j][i] = sum;
            }
        }

        try {
            // compute the covariances matrix
            return new RealMatrixImpl(jTj).inverse().getData();
        } catch (InvalidMatrixException ime) {
            throw new EstimationException("unable to compute covariances: singular problem",
                                          new Object[0]);
        }

    }