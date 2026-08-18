// org/apache/commons/math/estimation/GaussNewtonEstimatorTest.java
public void testAllBoundParameters() throws EstimationException {
      EstimatedParameter[] p = {
        new EstimatedParameter("bound0", 1, true),
        new EstimatedParameter("bound1", 2, true)
      };
      LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
        new LinearMeasurement(new double[] { 1.0, 1.0 },
                              new EstimatedParameter[] { p[0], p[1] },
                              3.0),
        new LinearMeasurement(new double[] { 2.0, -1.0 },
                              new EstimatedParameter[] { p[0], p[1] },
                              1.0),
        new LinearMeasurement(new double[] { 1.0, 3.0 },
                              new EstimatedParameter[] { p[0], p[1] },
                              7.0)
      });

      GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
      estimator.estimate(problem);
      double[][] covariances = estimator.getCovariances(problem);
      assertEquals(0, covariances.length);
      assertEquals(0, covariances[0].length);

      double[] errors = estimator.guessParametersErrors(problem);
      assertEquals(0, errors.length);
  }