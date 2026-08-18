// org/apache/commons/math/optimization/direct/MultiDirectionalTest.java
@Test
  public void testConstantFunction()
      throws FunctionEvaluationException, OptimizationException {
      MultiDirectional optimizer = new MultiDirectional();
      optimizer.setMaxIterations(100);
      optimizer.setMaxEvaluations(1000);
      MultivariateRealFunction constant = new MultivariateRealFunction() {
          public double value(double[] variables) {
              return 1.0;
          }
      };
      RealPointValuePair result = optimizer.optimize(constant, GoalType.MINIMIZE, new double[] { 5.0 });
      Assert.assertEquals(1.0, result.getValue(), 1e-10);
  }
