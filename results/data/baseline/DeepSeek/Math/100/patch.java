public double[][] getCovariances(EstimationProblem problem)
      throws EstimationException {
 
        // set up the jacobian
        updateJacobian();

        // compute transpose(J).J, avoiding building big intermediate matrices
        final int rows = problem.getMeasurements().length;
        final int cols = problem.getAllParameters().length;
        double[][] jTj = new double[cols][cols];
        for (int i = 0; i < cols; ++i) {
            for (int j = i; j < cols; ++j) {
                double sum = 0;
                // Jacobian is stored column-major: element (row, col) is at jacobian[col * rows + row]
                for (int k = 0; k < rows; ++k) {
                    sum += jacobian[i * rows + k] * jacobian[j * rows + k];
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