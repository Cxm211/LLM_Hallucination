// org/apache/commons/math/optimization/direct/MultiDirectionalTest.java
@Test
  public void testConvergenceOnSinglePoint()
      throws FunctionEvaluationException, OptimizationException {
      // Test case where the function has a very flat region
      // This tests the shrink path when reflection and contraction both fail
      MultiDirectional multiDirectional = new MultiDirectional();
      multiDirectional.setMaxIterations(150);
      multiDirectional.setMaxEvaluations(500);
      multiDirectional.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-10, 1.0e-30));

      MultivariateRealFunction flatFunction = new MultivariateRealFunction() {
          private static final long serialVersionUID = 1L;
          public double value(double[] variables) throws FunctionEvaluationException {
              double x = variables[0];
              double y = variables[1];
              // Function with a very flat minimum at (0,0)
              return x * x + y * y + 0.0001 * Math.sin(10 * x) * Math.sin(10 * y);
          }
      };

      RealPointValuePair estimate = multiDirectional.optimize(flatFunction,
                                    GoalType.MINIMIZE, new double[] { 1.5, 1.5 });

      final double EPSILON = 1e-4;
      Assert.assertEquals(0.0, estimate.getPoint()[0], EPSILON);
      Assert.assertEquals(0.0, estimate.getPoint()[1], EPSILON);
      Assert.assertEquals(0.0, estimate.getValue(), EPSILON);
  }