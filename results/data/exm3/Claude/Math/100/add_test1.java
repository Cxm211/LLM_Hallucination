// org/apache/commons/math/estimation/GaussNewtonEstimatorTest.java
public void testSingleUnboundParameter() throws EstimationException {
      EstimatedParameter[] p = {
        new EstimatedParameter("unbound", 2, false),
        new EstimatedParameter("bound0", 1, true),
        new EstimatedParameter("bound1", 3, true)
      };
      LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
        new LinearMeasurement(new double[] { 2.0, 0, 0 },
                              new EstimatedParameter[] { p[0], p[1], p[2] },
                              4.0),
        new LinearMeasurement(new double[] { 3.0, 0, 0 },
                              new EstimatedParameter[] { p[0], p[1], p[2] },
                              6.0),
        new LinearMeasurement(new double[] { 1.0, 0, 0 },
                              new EstimatedParameter[] { p[0], p[1], p[2] },
                              2.0)
      });

      GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
      estimator.estimate(problem);
      double[][] covariances = estimator.getCovariances(problem);
      assertEquals(1, covariances.length);
      assertEquals(1, covariances[0].length);
      assertTrue(covariances[0][0] > 0);

      double[] errors = estimator.guessParametersErrors(problem);
      assertEquals(1, errors.length);
      assertEquals(0, errors[0], 1.0e-10);
  }