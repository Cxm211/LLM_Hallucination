// buggy code
    protected void iterateSimplex(final Comparator<RealPointValuePair> comparator)
        throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {

        while (true) {

            incrementIterationsCounter();

            // save the original vertex
            final RealPointValuePair[] original = simplex;
            final RealPointValuePair best = original[0];

            // perform a reflection step
            final RealPointValuePair reflected = evaluateNewSimplex(original, 1.0, comparator);
            if (comparator.compare(reflected, best) < 0) {

                // compute the expanded simplex
                final RealPointValuePair[] reflectedSimplex = simplex;
                final RealPointValuePair expanded = evaluateNewSimplex(original, khi, comparator);
                if (comparator.compare(reflected, expanded) <= 0) {
                    // accept the reflected simplex
                    simplex = reflectedSimplex;
                }

                return;

            }

            // compute the contracted simplex
            final RealPointValuePair contracted = evaluateNewSimplex(original, gamma, comparator);
            if (comparator.compare(contracted, best) < 0) {
                // accept the contracted simplex

            // check convergence
                return;
            }

        }

    }

// relevant test
// org.apache.commons.math.optimization.direct.MultiDirectionalTest::testFunctionEvaluationExceptions
  public void testFunctionEvaluationExceptions() {
      MultivariateRealFunction wrong =
          new MultivariateRealFunction() {
            private static final long serialVersionUID = 4751314470965489371L;
            public double value(double[] x) throws FunctionEvaluationException {
                if (x[0] < 0) {
                    throw new FunctionEvaluationException(x, "{0}", "oops");
                } else if (x[0] > 1) {
                    throw new FunctionEvaluationException(new RuntimeException("oops"), x);
                } else {
                    return x[0] * (1 - x[0]);
                }
            }
      };
      try {
          MultiDirectional optimizer = new MultiDirectional(0.9, 1.9);
          optimizer.optimize(wrong, GoalType.MINIMIZE, new double[] { -1.0 });
          Assert.fail("an exception should have been thrown");
      } catch (FunctionEvaluationException ce) {
          
          Assert.assertNull(ce.getCause());
      } catch (Exception e) {
          Assert.fail("wrong exception caught: " + e.getMessage());
      } 
      try {
          MultiDirectional optimizer = new MultiDirectional(0.9, 1.9);
          optimizer.optimize(wrong, GoalType.MINIMIZE, new double[] { +2.0 });
          Assert.fail("an exception should have been thrown");
      } catch (FunctionEvaluationException ce) {
          
          Assert.assertNotNull(ce.getCause());
      } catch (Exception e) {
          Assert.fail("wrong exception caught: " + e.getMessage());
      } 
  }

// org.apache.commons.math.optimization.direct.MultiDirectionalTest::testMinimizeMaximize
  public void testMinimizeMaximize()
      throws FunctionEvaluationException, ConvergenceException {

      
      final double xM        = -3.841947088256863675365;
      final double yM        = -1.391745200270734924416;
      final double xP        =  0.2286682237349059125691;
      final double yP        = -yM;
      final double valueXmYm =  0.2373295333134216789769; 
      final double valueXmYp = -valueXmYm;                
      final double valueXpYm = -0.7290400707055187115322; 
      final double valueXpYp = -valueXpYm;                
      MultivariateRealFunction fourExtrema = new MultivariateRealFunction() {
          private static final long serialVersionUID = -7039124064449091152L;
          public double value(double[] variables) throws FunctionEvaluationException {
              final double x = variables[0];
              final double y = variables[1];
              return ((x == 0) || (y == 0)) ? 0 : (Math.atan(x) * Math.atan(x + 2) * Math.atan(y) * Math.atan(y) / (x * y));
          }
      };

      MultiDirectional optimizer = new MultiDirectional();
      optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-11, 1.0e-30));
      optimizer.setMaxIterations(200);
      optimizer.setStartConfiguration(new double[] { 0.2, 0.2 });
      RealPointValuePair optimum;

      
      optimum = optimizer.optimize(fourExtrema, GoalType.MINIMIZE, new double[] { -3.0, 0 });
      Assert.assertEquals(xM,        optimum.getPoint()[0], 4.0e-6);
      Assert.assertEquals(yP,        optimum.getPoint()[1], 3.0e-6);
      Assert.assertEquals(valueXmYp, optimum.getValue(),    8.0e-13);
      Assert.assertTrue(optimizer.getEvaluations() > 120);
      Assert.assertTrue(optimizer.getEvaluations() < 150);

      optimum = optimizer.optimize(fourExtrema, GoalType.MINIMIZE, new double[] { +1, 0 });
      Assert.assertEquals(xP,        optimum.getPoint()[0], 2.0e-8);
      Assert.assertEquals(yM,        optimum.getPoint()[1], 3.0e-6);
      Assert.assertEquals(valueXpYm, optimum.getValue(),    2.0e-12);              
      Assert.assertTrue(optimizer.getEvaluations() > 120);
      Assert.assertTrue(optimizer.getEvaluations() < 150);

      
      optimum = optimizer.optimize(fourExtrema, GoalType.MAXIMIZE, new double[] { -3.0, 0.0 });
      Assert.assertEquals(xM,        optimum.getPoint()[0], 7.0e-7);
      Assert.assertEquals(yM,        optimum.getPoint()[1], 3.0e-7);
      Assert.assertEquals(valueXmYm, optimum.getValue(),    2.0e-14);
      Assert.assertTrue(optimizer.getEvaluations() > 120);
      Assert.assertTrue(optimizer.getEvaluations() < 150);

      optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-15, 1.0e-30));
      optimum = optimizer.optimize(fourExtrema, GoalType.MAXIMIZE, new double[] { +1, 0 });
      Assert.assertEquals(xP,        optimum.getPoint()[0], 2.0e-8);
      Assert.assertEquals(yP,        optimum.getPoint()[1], 3.0e-6);
      Assert.assertEquals(valueXpYp, optimum.getValue(),    2.0e-12);
      Assert.assertTrue(optimizer.getEvaluations() > 180);
      Assert.assertTrue(optimizer.getEvaluations() < 220);

  }

// org.apache.commons.math.optimization.direct.MultiDirectionalTest::testRosenbrock
  public void testRosenbrock()
    throws FunctionEvaluationException, ConvergenceException {

    MultivariateRealFunction rosenbrock =
      new MultivariateRealFunction() {
        private static final long serialVersionUID = -9044950469615237490L;
        public double value(double[] x) throws FunctionEvaluationException {
          ++count;
          double a = x[1] - x[0] * x[0];
          double b = 1.0 - x[0];
          return 100 * a * a + b * b;
        }
      };

    count = 0;
    MultiDirectional optimizer = new MultiDirectional();
    optimizer.setConvergenceChecker(new SimpleScalarValueChecker(-1, 1.0e-3));
    optimizer.setMaxIterations(100);
    optimizer.setStartConfiguration(new double[][] {
            { -1.2,  1.0 }, { 0.9, 1.2 } , {  3.5, -2.3 }
    });
    RealPointValuePair optimum =
        optimizer.optimize(rosenbrock, GoalType.MINIMIZE, new double[] { -1.2, 1.0 });

    Assert.assertEquals(count, optimizer.getEvaluations());
    Assert.assertTrue(optimizer.getEvaluations() > 50);
    Assert.assertTrue(optimizer.getEvaluations() < 100);
    Assert.assertTrue(optimum.getValue() > 1.0e-2);

  }

// org.apache.commons.math.optimization.direct.MultiDirectionalTest::testPowell
  public void testPowell()
    throws FunctionEvaluationException, ConvergenceException {

    MultivariateRealFunction powell =
      new MultivariateRealFunction() {
        private static final long serialVersionUID = -832162886102041840L;
        public double value(double[] x) throws FunctionEvaluationException {
          ++count;
          double a = x[0] + 10 * x[1];
          double b = x[2] - x[3];
          double c = x[1] - 2 * x[2];
          double d = x[0] - x[3];
          return a * a + 5 * b * b + c * c * c * c + 10 * d * d * d * d;
        }
      };

    count = 0;
    MultiDirectional optimizer = new MultiDirectional();
    optimizer.setConvergenceChecker(new SimpleScalarValueChecker(-1.0, 1.0e-3));
    optimizer.setMaxIterations(1000);
    RealPointValuePair optimum =
      optimizer.optimize(powell, GoalType.MINIMIZE, new double[] { 3.0, -1.0, 0.0, 1.0 });
    Assert.assertEquals(count, optimizer.getEvaluations());
    Assert.assertTrue(optimizer.getEvaluations() > 800);
    Assert.assertTrue(optimizer.getEvaluations() < 900);
    Assert.assertTrue(optimum.getValue() > 1.0e-2);

  }

// org.apache.commons.math.optimization.direct.MultiDirectionalTest::testMath283
  public void testMath283()
      throws FunctionEvaluationException, OptimizationException {
      
      
      MultiDirectional multiDirectional = new MultiDirectional();
      multiDirectional.setMaxIterations(100);
      multiDirectional.setMaxEvaluations(1000);

      final Gaussian2D function = new Gaussian2D(0.0, 0.0, 1.0);

      RealPointValuePair estimate = multiDirectional.optimize(function,
                                    GoalType.MAXIMIZE, function.getMaximumPosition());

      final double EPSILON = 1e-5;

      final double expectedMaximum = function.getMaximum();
      final double actualMaximum = estimate.getValue();
      Assert.assertEquals(expectedMaximum, actualMaximum, EPSILON);

      final double[] expectedPosition = function.getMaximumPosition();
      final double[] actualPosition = estimate.getPoint();
      Assert.assertEquals(expectedPosition[0], actualPosition[0], EPSILON );
      Assert.assertEquals(expectedPosition[1], actualPosition[1], EPSILON );
      
  }
