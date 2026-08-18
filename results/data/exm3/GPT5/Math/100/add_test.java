// org/apache/commons/math/estimation/GaussNewtonEstimatorTest.java::testGuessParametersErrorsWithResiduals
public void testGuessParametersErrorsWithResiduals() throws EstimationException {
      EstimatedParameter[] p = {
        new EstimatedParameter("u0", 0.0, false),
        new EstimatedParameter("b",  0.0, true),
        new EstimatedParameter("u1", 0.0, false)
      };
      LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
        new LinearMeasurement(new double[] { 1.0, 0.0,  1.0 }, new EstimatedParameter[] { p[0], p[1], p[2] },  2.0),
        new LinearMeasurement(new double[] { 2.0, 0.0, -1.0 }, new EstimatedParameter[] { p[0], p[1], p[2] },  0.5),
        new LinearMeasurement(new double[] { -1.0, 0.0, 2.0 }, new EstimatedParameter[] { p[0], p[1], p[2] },  1.0)
      });

      GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
      estimator.estimate(problem);

      double[][] covariances = estimator.getCovariances(problem);
      assertEquals(2, covariances.length);
      assertEquals(2, covariances[0].length);

      double[] errors = estimator.guessParametersErrors(problem);
      assertEquals(2, errors.length);
      assertTrue(errors[0] >= 0.0);
      assertTrue(errors[1] >= 0.0);
      assertTrue(errors[0] + errors[1] > 0.0);
  }