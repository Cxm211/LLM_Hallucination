// org/apache/commons/math/estimation/GaussNewtonEstimatorTest.java
public void testBoundParametersNonConsecutive() throws EstimationException {
      EstimatedParameter[] p = {
        new EstimatedParameter("p0", 2.0, false),
        new EstimatedParameter("p1", 2.0, true), // bound parameter in the middle
        new EstimatedParameter("p2", 2.0, false)
      };
      LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
        new LinearMeasurement(new double[] { 1.0, 0.0, 1.0 },
                              new EstimatedParameter[] { p[0], p[1], p[2] },
                              3.0),
        new LinearMeasurement(new double[] { 1.0, 0.0, -1.0 },
                              new EstimatedParameter[] { p[0], p[1], p[2] },
                              1.0)
      });

      GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
      estimator.estimate(problem);
      double[][] covariances = estimator.getCovariances(problem);
      assertEquals(2, covariances.length);
      assertEquals(2, covariances[0].length);
      double[] errors = estimator.guessParametersErrors(problem);
      assertEquals(2, errors.length);
  }
