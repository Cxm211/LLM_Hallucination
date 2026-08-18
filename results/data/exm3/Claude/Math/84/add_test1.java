// org/apache/commons/math/optimization/direct/MultiDirectionalTest.java
@Test
  public void testQuadraticMinimization()
      throws FunctionEvaluationException, OptimizationException {
      // Test a simple quadratic function to ensure the shrink path works correctly
      MultiDirectional multiDirectional = new MultiDirectional();
      multiDirectional.setMaxIterations(100);
      multiDirectional.setMaxEvaluations(500);
      multiDirectional.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-10, 1.0e-30));

      MultivariateRealFunction quadratic = new MultivariateRealFunction() {
          private static final long serialVersionUID = 1L;
          public double value(double[] variables) throws FunctionEvaluationException {
              double x = variables[0] - 3.0;
              double y = variables[1] + 2.0;
              return x * x + y * y;
          }
      };

      RealPointValuePair estimate = multiDirectional.optimize(quadratic,
                                    GoalType.MINIMIZE, new double[] { 0.0, 0.0 });

      final double EPSILON = 1e-6;
      Assert.assertEquals(3.0, estimate.getPoint()[0], EPSILON);
      Assert.assertEquals(-2.0, estimate.getPoint()[1], EPSILON);
      Assert.assertEquals(0.0, estimate.getValue(), EPSILON);
  }