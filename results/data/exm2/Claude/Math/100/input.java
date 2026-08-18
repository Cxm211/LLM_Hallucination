    public double[][] getCovariances(EstimationProblem problem)
      throws EstimationException {
 
        // set up the jacobian
        updateJacobian();

        // compute transpose(J).J, avoiding building big intermediate matrices
        final int rows = problem.getMeasurements().length;
        final int cols = problem.getAllParameters().length;
        final int max  = cols * rows;
        double[][] jTj = new double[cols][cols];
        for (int i = 0; i < cols; ++i) {
            for (int j = i; j < cols; ++j) {
                double sum = 0;
                for (int k = 0; k < max; k += cols) {
                    sum += jacobian[k + i] * jacobian[k + j];
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

    public double[] guessParametersErrors(EstimationProblem problem)
      throws EstimationException {
        int m = problem.getMeasurements().length;
        int p = problem.getAllParameters().length;
        if (m <= p) {
            throw new EstimationException("no degrees of freedom ({0} measurements, {1} parameters)",
                                          new Object[] { new Integer(m), new Integer(p)});
        }
        double[] errors = new double[problem.getAllParameters().length];
        final double c = Math.sqrt(getChiSquare(problem) / (m - p));
        double[][] covar = getCovariances(problem);
        for (int i = 0; i < errors.length; ++i) {
            errors[i] = Math.sqrt(covar[i][i]) * c;
        }
        return errors;
    }

// trigger testcase
public void testBoundParameters() throws EstimationException {
      EstimatedParameter[] p = {
        new EstimatedParameter("unbound0", 2, false),
        new EstimatedParameter("unbound1", 2, false),
        new EstimatedParameter("bound",    2, true)
      };
      LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
        new LinearMeasurement(new double[] { 1.0, 1.0, 1.0 },
                              new EstimatedParameter[] { p[0], p[1], p[2] },
                              3.0),
        new LinearMeasurement(new double[] { 1.0, -1.0, 1.0 },
                              new EstimatedParameter[] { p[0], p[1], p[2] },
                              1.0),
        new LinearMeasurement(new double[] { 1.0, 3.0, 2.0 },
                              new EstimatedParameter[] { p[0], p[1], p[2] },
                              7.0)
      });

      GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
      estimator.estimate(problem);
      assertTrue(estimator.getRMS(problem) < 1.0e-10);
      double[][] covariances = estimator.getCovariances(problem);
      int i0 = 0, i1 = 1;
      if (problem.getUnboundParameters()[0].getName().endsWith("1")) {
          i0 = 1;
          i1 = 0;
      }
      assertEquals(11.0 / 24, covariances[i0][i0], 1.0e-10);
      assertEquals(-3.0 / 24, covariances[i0][i1], 1.0e-10);
      assertEquals(-3.0 / 24, covariances[i1][i0], 1.0e-10);
      assertEquals( 3.0 / 24, covariances[i1][i1], 1.0e-10);

      double[] errors = estimator.guessParametersErrors(problem);
      assertEquals(0, errors[i0], 1.0e-10);
      assertEquals(0, errors[i1], 1.0e-10);

  }
