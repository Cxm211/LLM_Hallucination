// org/apache/commons/math/optimization/direct/MultiDirectionalTest.java::testMath283
@Test
  public void testNoImprovement()
      throws FunctionEvaluationException, OptimizationException {

      // constant function: reflection and contraction never improve
      MultivariateRealFunction constant = new MultivariateRealFunction() {
          private static final long serialVersionUID = 1L;
          public double value(double[] variables) throws FunctionEvaluationException {
              return 0.0;
          }
      };

      MultiDirectional optimizer = new MultiDirectional();
      optimizer.setMaxIterations(50);
      optimizer.setMaxEvaluations(200);

      RealPointValuePair optimum = optimizer.optimize(constant, GoalType.MAXIMIZE, new double[] { 1.0, -2.0 });

      Assert.assertEquals(0.0, optimum.getValue(), 0.0);
  }