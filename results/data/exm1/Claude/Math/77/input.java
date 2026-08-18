// buggy code
    public double getLInfNorm() {
        double max = 0;
        for (double a : data) {
            max += Math.max(max, Math.abs(a));
        }
        return max;
    }

    public double getLInfNorm() {
        double max = 0;
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            max += iter.value();
        }
        return max;
    }

// relevant test
// org.apache.commons.math.FunctionEvaluationExceptionTest::testConstructor
    public void testConstructor(){
        FunctionEvaluationException ex = new FunctionEvaluationException(0.0);
        assertNull(ex.getCause());
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().indexOf("0") > 0);
        assertEquals(0.0, ex.getArgument()[0], 0);
    }

// org.apache.commons.math.FunctionEvaluationExceptionTest::testConstructorArray
    public void testConstructorArray(){
        FunctionEvaluationException ex =
            new FunctionEvaluationException(new double[] { 0, 1, 2 });
        assertNull(ex.getCause());
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().indexOf("0") > 0);
        assertEquals(0.0, ex.getArgument()[0], 0);
        assertEquals(1.0, ex.getArgument()[1], 0);
        assertEquals(2.0, ex.getArgument()[2], 0);
    }

// org.apache.commons.math.FunctionEvaluationExceptionTest::testConstructorPatternArguments
    public void testConstructorPatternArguments(){
        String pattern = "evaluation failed for argument = {0}";
        Object[] arguments = { Double.valueOf(0.0) };
        FunctionEvaluationException ex = new FunctionEvaluationException(0.0, pattern, arguments);
        assertNull(ex.getCause());
        assertEquals(pattern, ex.getPattern());
        assertEquals(arguments.length, ex.getArguments().length);
        for (int i = 0; i < arguments.length; ++i) {
            assertEquals(arguments[i], ex.getArguments()[i]);
        }
        assertFalse(pattern.equals(ex.getMessage()));
        assertFalse(ex.getMessage().equals(ex.getMessage(Locale.FRENCH)));
    }

// org.apache.commons.math.FunctionEvaluationExceptionTest::testConstructorArrayPatternArguments
    public void testConstructorArrayPatternArguments(){
        String pattern = "evaluation failed for argument = {0}";
        Object[] arguments = { Double.valueOf(0.0) };
        FunctionEvaluationException ex =
            new FunctionEvaluationException(new double[] { 0, 1, 2 }, pattern, arguments);
        assertNull(ex.getCause());
        assertEquals(pattern, ex.getPattern());
        assertEquals(arguments.length, ex.getArguments().length);
        for (int i = 0; i < arguments.length; ++i) {
            assertEquals(arguments[i], ex.getArguments()[i]);
        }
        assertFalse(pattern.equals(ex.getMessage()));
        assertFalse(ex.getMessage().equals(ex.getMessage(Locale.FRENCH)));
        assertEquals(0.0, ex.getArgument()[0], 0);
        assertEquals(1.0, ex.getArgument()[1], 0);
        assertEquals(2.0, ex.getArgument()[2], 0);
    }

// org.apache.commons.math.FunctionEvaluationExceptionTest::testConstructorPatternArgumentsCause
    public void testConstructorPatternArgumentsCause(){
        String pattern = "evaluation failed for argument = {0}";
        Object[] arguments = { Double.valueOf(0.0) };
        String inMsg = "inner message";
        Exception cause = new Exception(inMsg);
        FunctionEvaluationException ex = new FunctionEvaluationException(cause, 0.0, pattern, arguments);
        assertEquals(cause, ex.getCause());
        assertEquals(pattern, ex.getPattern());
        assertEquals(arguments.length, ex.getArguments().length);
        for (int i = 0; i < arguments.length; ++i) {
            assertEquals(arguments[i], ex.getArguments()[i]);
        }
        assertFalse(pattern.equals(ex.getMessage()));
        assertFalse(ex.getMessage().equals(ex.getMessage(Locale.FRENCH)));
    }

// org.apache.commons.math.FunctionEvaluationExceptionTest::testConstructorArrayPatternArgumentsCause
    public void testConstructorArrayPatternArgumentsCause(){
        String pattern = "evaluation failed for argument = {0}";
        Object[] arguments = { Double.valueOf(0.0) };
        String inMsg = "inner message";
        Exception cause = new Exception(inMsg);
        FunctionEvaluationException ex =
            new FunctionEvaluationException(cause, new double[] { 0, 1, 2 }, pattern, arguments);
        assertEquals(cause, ex.getCause());
        assertEquals(pattern, ex.getPattern());
        assertEquals(arguments.length, ex.getArguments().length);
        for (int i = 0; i < arguments.length; ++i) {
            assertEquals(arguments[i], ex.getArguments()[i]);
        }
        assertFalse(pattern.equals(ex.getMessage()));
        assertFalse(ex.getMessage().equals(ex.getMessage(Locale.FRENCH)));
        assertEquals(0.0, ex.getArgument()[0], 0);
        assertEquals(1.0, ex.getArgument()[1], 0);
        assertEquals(2.0, ex.getArgument()[2], 0);
    }

// org.apache.commons.math.FunctionEvaluationExceptionTest::testConstructorArgumentCause
    public void testConstructorArgumentCause(){
        String inMsg = "inner message";
        Exception cause = new Exception(inMsg);
        FunctionEvaluationException ex = new FunctionEvaluationException(cause, 0.0);
        assertEquals(cause, ex.getCause());
        assertTrue(ex.getMessage().equals(ex.getMessage(Locale.FRENCH)));
    }

// org.apache.commons.math.FunctionEvaluationExceptionTest::testConstructorArrayArgumentCause
    public void testConstructorArrayArgumentCause(){
        String inMsg = "inner message";
        Exception cause = new Exception(inMsg);
        FunctionEvaluationException ex =
            new FunctionEvaluationException(cause, new double[] { 0, 1, 2 });
        assertEquals(cause, ex.getCause());
        assertTrue(ex.getMessage().equals(ex.getMessage(Locale.FRENCH)));
        assertEquals(0.0, ex.getArgument()[0], 0);
        assertEquals(1.0, ex.getArgument()[1], 0);
        assertEquals(2.0, ex.getArgument()[2], 0);
    }

// org.apache.commons.math.analysis.interpolation.MicrosphereInterpolatorTest::testLinearFunction2D
    public void testLinearFunction2D() throws MathException {
        MultivariateRealFunction f = new MultivariateRealFunction() {
                public double value(double[] x) {
                    if (x.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    return 2 * x[0] - 3 * x[1] + 5;
                }
            };

        MultivariateRealInterpolator interpolator = new MicrosphereInterpolator();

        
        final int n = 9;
        final int dim = 2;
        double[][] x = new double[n][dim];
        double[] y = new double[n];
        int index = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                x[index][0] = i;
                x[index][1] = j;
                y[index] = f.value(x[index]);
                ++index;
            }
        }

        MultivariateRealFunction p = interpolator.interpolate(x, y);

        double[] c = new double[dim];
        double expected, result;

        c[0] = 0;
        c[1] = 0;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("On sample point", expected, result, Math.ulp(1d));

        c[0] = 0 + 1e-5;
        c[1] = 1 - 1e-5;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("1e-5 away from sample point", expected, result, 1e-4);
    }

// org.apache.commons.math.analysis.interpolation.MicrosphereInterpolatorTest::testParaboloid2D
    public void testParaboloid2D() throws MathException {
        MultivariateRealFunction f = new MultivariateRealFunction() {
                public double value(double[] x) {
                    if (x.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    return 2 * x[0] * x[0] - 3 * x[1] * x[1] + 4 * x[0] * x[1] - 5;
                }
            };

        MultivariateRealInterpolator interpolator = new MicrosphereInterpolator();

        
        final int n = 121;
        final int dim = 2;
        double[][] x = new double[n][dim];
        double[] y = new double[n];
        int index = 0;
        for (int i = -10; i <= 10; i += 2) {
            for (int j = -10; j <= 10; j += 2) {
                x[index][0] = i;
                x[index][1] = j;
                y[index] = f.value(x[index]);
                ++index;
            }
        }

        MultivariateRealFunction p = interpolator.interpolate(x, y);

        double[] c = new double[dim];
        double expected, result;

        c[0] = 0;
        c[1] = 0;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("On sample point", expected, result, Math.ulp(1d));

        c[0] = 2 + 1e-5;
        c[1] = 2 - 1e-5;
        expected = f.value(c);
        result = p.value(c);
        Assert.assertEquals("1e-5 away from sample point", expected, result, 1e-3);
    }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testTrivial
  public void testTrivial() throws EstimationException {
    LinearProblem problem =
      new LinearProblem(new LinearMeasurement[] {
        new LinearMeasurement(new double[] {2},
                              new EstimatedParameter[] {
                                 new EstimatedParameter("p0", 0)
                              }, 3.0)
      });
    GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    assertEquals(1.5,
                 problem.getUnboundParameters()[0].getEstimate(),
                 1.0e-10);
   }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testQRColumnsPermutation
  public void testQRColumnsPermutation() throws EstimationException {

    EstimatedParameter[] x = {
       new EstimatedParameter("p0", 0), new EstimatedParameter("p1", 0)
    };
    LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
      new LinearMeasurement(new double[] { 1.0, -1.0 },
                            new EstimatedParameter[] { x[0], x[1] },
                            4.0),
      new LinearMeasurement(new double[] { 2.0 },
                            new EstimatedParameter[] { x[1] },
                            6.0),
      new LinearMeasurement(new double[] { 1.0, -2.0 },
                            new EstimatedParameter[] { x[0], x[1] },
                            1.0)
    });

    GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    assertEquals(7.0, x[0].getEstimate(), 1.0e-10);
    assertEquals(3.0, x[1].getEstimate(), 1.0e-10);

  }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testNoDependency
  public void testNoDependency() throws EstimationException {
    EstimatedParameter[] p = new EstimatedParameter[] {
      new EstimatedParameter("p0", 0),
      new EstimatedParameter("p1", 0),
      new EstimatedParameter("p2", 0),
      new EstimatedParameter("p3", 0),
      new EstimatedParameter("p4", 0),
      new EstimatedParameter("p5", 0)
    };
    LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
      new LinearMeasurement(new double[] {2}, new EstimatedParameter[] { p[0] }, 0.0),
      new LinearMeasurement(new double[] {2}, new EstimatedParameter[] { p[1] }, 1.1),
      new LinearMeasurement(new double[] {2}, new EstimatedParameter[] { p[2] }, 2.2),
      new LinearMeasurement(new double[] {2}, new EstimatedParameter[] { p[3] }, 3.3),
      new LinearMeasurement(new double[] {2}, new EstimatedParameter[] { p[4] }, 4.4),
      new LinearMeasurement(new double[] {2}, new EstimatedParameter[] { p[5] }, 5.5)
    });
  GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
  estimator.estimate(problem);
  assertEquals(0, estimator.getRMS(problem), 1.0e-10);
  for (int i = 0; i < p.length; ++i) {
    assertEquals(0.55 * i, p[i].getEstimate(), 1.0e-10);
  }
}

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testOneSet
  public void testOneSet() throws EstimationException {

    EstimatedParameter[] p = {
       new EstimatedParameter("p0", 0),
       new EstimatedParameter("p1", 0),
       new EstimatedParameter("p2", 0)
    };
    LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
      new LinearMeasurement(new double[] { 1.0 },
                            new EstimatedParameter[] { p[0] },
                            1.0),
      new LinearMeasurement(new double[] { -1.0, 1.0 },
                            new EstimatedParameter[] { p[0], p[1] },
                            1.0),
      new LinearMeasurement(new double[] { -1.0, 1.0 },
                            new EstimatedParameter[] { p[1], p[2] },
                            1.0)
    });

    GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    assertEquals(1.0, p[0].getEstimate(), 1.0e-10);
    assertEquals(2.0, p[1].getEstimate(), 1.0e-10);
    assertEquals(3.0, p[2].getEstimate(), 1.0e-10);

  }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testTwoSets
  public void testTwoSets() throws EstimationException {
    EstimatedParameter[] p = {
      new EstimatedParameter("p0", 0),
      new EstimatedParameter("p1", 1),
      new EstimatedParameter("p2", 2),
      new EstimatedParameter("p3", 3),
      new EstimatedParameter("p4", 4),
      new EstimatedParameter("p5", 5)
    };

    double epsilon = 1.0e-7;
    LinearProblem problem = new LinearProblem(new LinearMeasurement[] {

      
      new LinearMeasurement(new double[] {  2.0,  1.0,  4.0 },
                            new EstimatedParameter[] { p[0], p[1], p[3] },
                            2.0),
      new LinearMeasurement(new double[] { -4.0, -2.0,   3.0, -7.0 },
                           new EstimatedParameter[] { p[0], p[1], p[2], p[3] },
                           -9.0),
      new LinearMeasurement(new double[] {  4.0,  1.0,  -2.0,  8.0 },
                            new EstimatedParameter[] { p[0], p[1], p[2], p[3] },
                            2.0),
      new LinearMeasurement(new double[] { -3.0, -12.0, -1.0 },
                           new EstimatedParameter[] { p[1], p[2], p[3] },
                           2.0),

      
      new LinearMeasurement(new double[] { epsilon, 1.0 },
                            new EstimatedParameter[] { p[4], p[5] },
                            1.0 + epsilon * epsilon),
      new LinearMeasurement(new double[] {  1.0, 1.0 },
                            new EstimatedParameter[] { p[4], p[5] },
                            2.0)

    });

    GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    assertEquals( 3.0, p[0].getEstimate(), 1.0e-10);
    assertEquals( 4.0, p[1].getEstimate(), 1.0e-10);
    assertEquals(-1.0, p[2].getEstimate(), 1.0e-10);
    assertEquals(-2.0, p[3].getEstimate(), 1.0e-10);
    assertEquals( 1.0 + epsilon, p[4].getEstimate(), 1.0e-10);
    assertEquals( 1.0 - epsilon, p[5].getEstimate(), 1.0e-10);

  }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testNonInversible
  public void testNonInversible() {

    EstimatedParameter[] p = {
       new EstimatedParameter("p0", 0),
       new EstimatedParameter("p1", 0),
       new EstimatedParameter("p2", 0)
    };
    LinearMeasurement[] m = new LinearMeasurement[] {
      new LinearMeasurement(new double[] {  1.0, 2.0, -3.0 },
                            new EstimatedParameter[] { p[0], p[1], p[2] },
                            1.0),
      new LinearMeasurement(new double[] {  2.0, 1.0,  3.0 },
                            new EstimatedParameter[] { p[0], p[1], p[2] },
                            1.0),
      new LinearMeasurement(new double[] { -3.0, -9.0 },
                            new EstimatedParameter[] { p[0], p[2] },
                            1.0)
    };
    LinearProblem problem = new LinearProblem(m);

    GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    try {
      estimator.estimate(problem);
      fail("an exception should have been caught");
    } catch (EstimationException ee) {
      
    } catch (Exception e) {
      fail("wrong exception type caught");
    }
  }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testIllConditioned
  public void testIllConditioned() throws EstimationException {
    EstimatedParameter[] p = {
      new EstimatedParameter("p0", 0),
      new EstimatedParameter("p1", 1),
      new EstimatedParameter("p2", 2),
      new EstimatedParameter("p3", 3)
    };

    LinearProblem problem1 = new LinearProblem(new LinearMeasurement[] {
      new LinearMeasurement(new double[] { 10.0, 7.0,  8.0,  7.0 },
                            new EstimatedParameter[] { p[0], p[1], p[2], p[3] },
                            32.0),
      new LinearMeasurement(new double[] {  7.0, 5.0,  6.0,  5.0 },
                            new EstimatedParameter[] { p[0], p[1], p[2], p[3] },
                            23.0),
      new LinearMeasurement(new double[] {  8.0, 6.0, 10.0,  9.0 },
                            new EstimatedParameter[] { p[0], p[1], p[2], p[3] },
                            33.0),
      new LinearMeasurement(new double[] {  7.0, 5.0,  9.0, 10.0 },
                            new EstimatedParameter[] { p[0], p[1], p[2], p[3] },
                            31.0)
    });
    GaussNewtonEstimator estimator1 = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    estimator1.estimate(problem1);
    assertEquals(0, estimator1.getRMS(problem1), 1.0e-10);
    assertEquals(1.0, p[0].getEstimate(), 1.0e-10);
    assertEquals(1.0, p[1].getEstimate(), 1.0e-10);
    assertEquals(1.0, p[2].getEstimate(), 1.0e-10);
    assertEquals(1.0, p[3].getEstimate(), 1.0e-10);

    LinearProblem problem2 = new LinearProblem(new LinearMeasurement[] {
      new LinearMeasurement(new double[] { 10.0, 7.0,  8.1,  7.2 },
                            new EstimatedParameter[] { p[0], p[1], p[2], p[3] },
                            32.0),
      new LinearMeasurement(new double[] {  7.08, 5.04,  6.0,  5.0 },
                            new EstimatedParameter[] { p[0], p[1], p[2], p[3] },
                            23.0),
      new LinearMeasurement(new double[] {  8.0, 5.98, 9.89,  9.0 },
                             new EstimatedParameter[] { p[0], p[1], p[2], p[3] },
                            33.0),
      new LinearMeasurement(new double[] {  6.99, 4.99,  9.0, 9.98 },
                             new EstimatedParameter[] { p[0], p[1], p[2], p[3] },
                            31.0)
    });
    GaussNewtonEstimator estimator2 = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    estimator2.estimate(problem2);
    assertEquals(0, estimator2.getRMS(problem2), 1.0e-10);
    assertEquals(-81.0, p[0].getEstimate(), 1.0e-8);
    assertEquals(137.0, p[1].getEstimate(), 1.0e-8);
    assertEquals(-34.0, p[2].getEstimate(), 1.0e-8);
    assertEquals( 22.0, p[3].getEstimate(), 1.0e-8);

  }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testMoreEstimatedParametersSimple
  public void testMoreEstimatedParametersSimple() {

    EstimatedParameter[] p = {
       new EstimatedParameter("p0", 7),
       new EstimatedParameter("p1", 6),
       new EstimatedParameter("p2", 5),
       new EstimatedParameter("p3", 4)
     };
    LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
      new LinearMeasurement(new double[] { 3.0, 2.0 },
                             new EstimatedParameter[] { p[0], p[1] },
                             7.0),
      new LinearMeasurement(new double[] { 1.0, -1.0, 1.0 },
                             new EstimatedParameter[] { p[1], p[2], p[3] },
                             3.0),
      new LinearMeasurement(new double[] { 2.0, 1.0 },
                             new EstimatedParameter[] { p[0], p[2] },
                             5.0)
    });

    GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    try {
        estimator.estimate(problem);
        fail("an exception should have been caught");
    } catch (EstimationException ee) {
        
    } catch (Exception e) {
        fail("wrong exception type caught");
    }

  }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testMoreEstimatedParametersUnsorted
  public void testMoreEstimatedParametersUnsorted() {
    EstimatedParameter[] p = {
      new EstimatedParameter("p0", 2),
      new EstimatedParameter("p1", 2),
      new EstimatedParameter("p2", 2),
      new EstimatedParameter("p3", 2),
      new EstimatedParameter("p4", 2),
      new EstimatedParameter("p5", 2)
    };
    LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
      new LinearMeasurement(new double[] { 1.0, 1.0 },
                           new EstimatedParameter[] { p[0], p[1] },
                           3.0),
      new LinearMeasurement(new double[] { 1.0, 1.0, 1.0 },
                           new EstimatedParameter[] { p[2], p[3], p[4] },
                           12.0),
      new LinearMeasurement(new double[] { 1.0, -1.0 },
                           new EstimatedParameter[] { p[4], p[5] },
                           -1.0),
      new LinearMeasurement(new double[] { 1.0, -1.0, 1.0 },
                           new EstimatedParameter[] { p[3], p[2], p[5] },
                           7.0),
      new LinearMeasurement(new double[] { 1.0, -1.0 },
                           new EstimatedParameter[] { p[4], p[3] },
                           1.0)
    });

    GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    try {
        estimator.estimate(problem);
        fail("an exception should have been caught");
    } catch (EstimationException ee) {
        
    } catch (Exception e) {
        fail("wrong exception type caught");
    }

  }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testRedundantEquations
  public void testRedundantEquations() throws EstimationException {
    EstimatedParameter[] p = {
      new EstimatedParameter("p0", 1),
      new EstimatedParameter("p1", 1)
    };
    LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
      new LinearMeasurement(new double[] { 1.0, 1.0 },
                             new EstimatedParameter[] { p[0], p[1] },
                             3.0),
      new LinearMeasurement(new double[] { 1.0, -1.0 },
                             new EstimatedParameter[] { p[0], p[1] },
                             1.0),
      new LinearMeasurement(new double[] { 1.0, 3.0 },
                             new EstimatedParameter[] { p[0], p[1] },
                             5.0)
    });

    GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    EstimatedParameter[] all = problem.getAllParameters();
    for (int i = 0; i < all.length; ++i) {
        assertEquals(all[i].getName().equals("p0") ? 2.0 : 1.0,
                     all[i].getEstimate(), 1.0e-10);
    }

  }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testInconsistentEquations
  public void testInconsistentEquations() throws EstimationException {
    EstimatedParameter[] p = {
      new EstimatedParameter("p0", 1),
      new EstimatedParameter("p1", 1)
    };
    LinearProblem problem = new LinearProblem(new LinearMeasurement[] {
      new LinearMeasurement(new double[] { 1.0, 1.0 },
                            new EstimatedParameter[] { p[0], p[1] },
                            3.0),
      new LinearMeasurement(new double[] { 1.0, -1.0 },
                            new EstimatedParameter[] { p[0], p[1] },
                            1.0),
      new LinearMeasurement(new double[] { 1.0, 3.0 },
                            new EstimatedParameter[] { p[0], p[1] },
                            4.0)
    });

    GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    estimator.estimate(problem);
    assertTrue(estimator.getRMS(problem) > 0.1);

  }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testBoundParameters
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

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testMaxIterations
  public void testMaxIterations() {
      Circle circle = new Circle(98.680, 47.345);
      circle.addPoint( 30.0,  68.0);
      circle.addPoint( 50.0,  -6.0);
      circle.addPoint(110.0, -20.0);
      circle.addPoint( 35.0,  15.0);
      circle.addPoint( 45.0,  97.0);
      try {
        GaussNewtonEstimator estimator = new GaussNewtonEstimator(4, 1.0e-14, 1.0e-14);
        estimator.estimate(circle);
        fail("an exception should have been caught");
      } catch (EstimationException ee) {
        
      } catch (Exception e) {
        fail("wrong exception type caught");
      }
    }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testCircleFitting
  public void testCircleFitting() throws EstimationException {
      Circle circle = new Circle(98.680, 47.345);
      circle.addPoint( 30.0,  68.0);
      circle.addPoint( 50.0,  -6.0);
      circle.addPoint(110.0, -20.0);
      circle.addPoint( 35.0,  15.0);
      circle.addPoint( 45.0,  97.0);
      GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-10, 1.0e-10);
      estimator.estimate(circle);
      double rms = estimator.getRMS(circle);
      assertEquals(1.768262623567235,  Math.sqrt(circle.getM()) * rms,  1.0e-10);
      assertEquals(69.96016176931406, circle.getRadius(), 1.0e-10);
      assertEquals(96.07590211815305, circle.getX(),      1.0e-10);
      assertEquals(48.13516790438953, circle.getY(),      1.0e-10);
    }

// org.apache.commons.math.estimation.GaussNewtonEstimatorTest::testCircleFittingBadInit
  public void testCircleFittingBadInit() {
    Circle circle = new Circle(-12, -12);
    double[][] points = new double[][] {
      {-0.312967,  0.072366}, {-0.339248,  0.132965}, {-0.379780,  0.202724},
      {-0.390426,  0.260487}, {-0.361212,  0.328325}, {-0.346039,  0.392619},
      {-0.280579,  0.444306}, {-0.216035,  0.470009}, {-0.149127,  0.493832},
      {-0.075133,  0.483271}, {-0.007759,  0.452680}, { 0.060071,  0.410235},
      { 0.103037,  0.341076}, { 0.118438,  0.273884}, { 0.131293,  0.192201},
      { 0.115869,  0.129797}, { 0.072223,  0.058396}, { 0.022884,  0.000718},
      {-0.053355, -0.020405}, {-0.123584, -0.032451}, {-0.216248, -0.032862},
      {-0.278592, -0.005008}, {-0.337655,  0.056658}, {-0.385899,  0.112526},
      {-0.405517,  0.186957}, {-0.415374,  0.262071}, {-0.387482,  0.343398},
      {-0.347322,  0.397943}, {-0.287623,  0.458425}, {-0.223502,  0.475513},
      {-0.135352,  0.478186}, {-0.061221,  0.483371}, { 0.003711,  0.422737},
      { 0.065054,  0.375830}, { 0.108108,  0.297099}, { 0.123882,  0.222850},
      { 0.117729,  0.134382}, { 0.085195,  0.056820}, { 0.029800, -0.019138},
      {-0.027520, -0.072374}, {-0.102268, -0.091555}, {-0.200299, -0.106578},
      {-0.292731, -0.091473}, {-0.356288, -0.051108}, {-0.420561,  0.014926},
      {-0.471036,  0.074716}, {-0.488638,  0.182508}, {-0.485990,  0.254068},
      {-0.463943,  0.338438}, {-0.406453,  0.404704}, {-0.334287,  0.466119},
      {-0.254244,  0.503188}, {-0.161548,  0.495769}, {-0.075733,  0.495560},
      { 0.001375,  0.434937}, { 0.082787,  0.385806}, { 0.115490,  0.323807},
      { 0.141089,  0.223450}, { 0.138693,  0.131703}, { 0.126415,  0.049174},
      { 0.066518, -0.010217}, {-0.005184, -0.070647}, {-0.080985, -0.103635},
      {-0.177377, -0.116887}, {-0.260628, -0.100258}, {-0.335756, -0.056251},
      {-0.405195, -0.000895}, {-0.444937,  0.085456}, {-0.484357,  0.175597},
      {-0.472453,  0.248681}, {-0.438580,  0.347463}, {-0.402304,  0.422428},
      {-0.326777,  0.479438}, {-0.247797,  0.505581}, {-0.152676,  0.519380},
      {-0.071754,  0.516264}, { 0.015942,  0.472802}, { 0.076608,  0.419077},
      { 0.127673,  0.330264}, { 0.159951,  0.262150}, { 0.153530,  0.172681},
      { 0.140653,  0.089229}, { 0.078666,  0.024981}, { 0.023807, -0.037022},
      {-0.048837, -0.077056}, {-0.127729, -0.075338}, {-0.221271, -0.067526}
    };
    for (int i = 0; i < points.length; ++i) {
      circle.addPoint(points[i][0], points[i][1]);
    }
    GaussNewtonEstimator estimator = new GaussNewtonEstimator(100, 1.0e-6, 1.0e-6);
    try {
        estimator.estimate(circle);
        fail("an exception should have been caught");
    } catch (EstimationException ee) {
        
    } catch (Exception e) {
        fail("wrong exception type caught");
    }
}

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testDimensions
    public void testDimensions() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Array2DRowRealMatrix m1 = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(m1.getData());
        assertEquals(m2,m1);
        Array2DRowRealMatrix m3 = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m4 = new Array2DRowRealMatrix(m3.getData(), false);
        assertEquals(m4,m3);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testAdd
    public void testAdd() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
                    testDataPlusInv[row][col],sumEntries[row][col],
                        entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testAddFail
    public void testAddFail() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testNorm
    public void testNorm() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        assertEquals("testData Frobenius norm", Math.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        assertEquals("testData2 Frobenius norm", Math.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testDataInv);
        TestUtils.assertEquals("m-n = m + -n",m.subtract(m2),
            m2.scalarMultiply(-1d).add(m),entryTolerance);
        try {
            m.subtract(new Array2DRowRealMatrix(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMultiply
     public void testMultiply() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        Array2DRowRealMatrix identity = new Array2DRowRealMatrix(id);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        TestUtils.assertEquals("inverse multiply",m.multiply(mInv),
            identity,entryTolerance);
        TestUtils.assertEquals("inverse multiply",mInv.multiply(m),
            identity,entryTolerance);
        TestUtils.assertEquals("identity multiply",m.multiply(identity),
            m,entryTolerance);
        TestUtils.assertEquals("identity multiply",identity.multiply(mInv),
            mInv,entryTolerance);
        TestUtils.assertEquals("identity multiply",m2.multiply(identity),
            m2,entryTolerance);
        try {
            m.multiply(new Array2DRowRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMultiply2
    public void testMultiply2() {
       RealMatrix m3 = new Array2DRowRealMatrix(d3);
       RealMatrix m4 = new Array2DRowRealMatrix(d4);
       RealMatrix m5 = new Array2DRowRealMatrix(d5);
       TestUtils.assertEquals("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = new Array2DRowRealMatrix(id);
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new Array2DRowRealMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("scalar add",new Array2DRowRealMatrix(testDataPlus2),
            m.scalarAdd(2d),entryTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testOperate
    public void testOperate() {
        RealMatrix m = new Array2DRowRealMatrix(id);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(testVector), entryTolerance);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(new ArrayRealVector(testVector)).getData(), entryTolerance);
        m = new Array2DRowRealMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        }, false);
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0], 1.0e-12);
        assertEquals( 7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals("inverse-transpose", mIT, mTI, normTolerance);
        m = new Array2DRowRealMatrix(testData2);
        RealMatrix mt = new Array2DRowRealMatrix(testData2T);
        TestUtils.assertEquals("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("premultiply", m.preMultiply(testVector),
                    preMultTest, normTolerance);
        TestUtils.assertEquals("premultiply", m.preMultiply(new ArrayRealVector(testVector).getData()),
                    preMultTest, normTolerance);
        m = new Array2DRowRealMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new Array2DRowRealMatrix(d3);
        RealMatrix m4 = new Array2DRowRealMatrix(d4);
        RealMatrix m5 = new Array2DRowRealMatrix(d5);
        TestUtils.assertEquals("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        Array2DRowRealMatrix identity = new Array2DRowRealMatrix(id);
        TestUtils.assertEquals("inverse multiply",m.preMultiply(mInv),
                identity,entryTolerance);
        TestUtils.assertEquals("inverse multiply",mInv.preMultiply(m),
                identity,entryTolerance);
        TestUtils.assertEquals("identity multiply",m.preMultiply(identity),
                m,entryTolerance);
        TestUtils.assertEquals("identity multiply",identity.preMultiply(mInv),
                mInv,entryTolerance);
        try {
            m.preMultiply(new Array2DRowRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("get row",m.getRow(0),testDataRow1,entryTolerance);
        TestUtils.assertEquals("get col",m.getColumn(2),testDataCol3,entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new Array2DRowRealMatrix(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new Array2DRowRealMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new Array2DRowRealMatrix(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);

    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkGetSubMatrix(m, subRows23Cols00,  2 , 3 , 0, 0, false);
        checkGetSubMatrix(m, subRows00Cols33,  0 , 0 , 3, 3, false);
        checkGetSubMatrix(m, subRows01Cols23,  0 , 1 , 2, 3, false);
        checkGetSubMatrix(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 },    false);
        checkGetSubMatrix(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 },    false);
        checkGetSubMatrix(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 }, false);
        checkGetSubMatrix(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 }, false);
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkGetSubMatrix(m, null,  1, 0, 2, 4, true);
        checkGetSubMatrix(m, null, -1, 1, 2, 2, true);
        checkGetSubMatrix(m, null,  1, 0, 2, 2, true);
        checkGetSubMatrix(m, null,  1, 0, 2, 4, true);
        checkGetSubMatrix(m, null, new int[] {},    new int[] { 0 }, true);
        checkGetSubMatrix(m, null, new int[] { 0 }, new int[] { 4 }, true);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkCopy(m, subRows23Cols00,  2 , 3 , 0, 0, false);
        checkCopy(m, subRows00Cols33,  0 , 0 , 3, 3, false);
        checkCopy(m, subRows01Cols23,  0 , 1 , 2, 3, false);
        checkCopy(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 },    false);
        checkCopy(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 },    false);
        checkCopy(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 }, false);
        checkCopy(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 }, false);
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);

        checkCopy(m, null,  1, 0, 2, 4, true);
        checkCopy(m, null, -1, 1, 2, 2, true);
        checkCopy(m, null,  1, 0, 2, 2, true);
        checkCopy(m, null,  1, 0, 2, 4, true);
        checkCopy(m, null, new int[] {},    new int[] { 0 }, true);
        checkCopy(m, null, new int[] { 0 }, new int[] { 4 }, true);
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mRow0 = new Array2DRowRealMatrix(subRow0);
        RealMatrix mRow3 = new Array2DRowRealMatrix(subRow3);
        assertEquals("Row0", mRow0,
                m.getRowMatrix(0));
        assertEquals("Row3", mRow3,
                m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mRow3 = new Array2DRowRealMatrix(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mColumn1 = new Array2DRowRealMatrix(subColumn1);
        RealMatrix mColumn3 = new Array2DRowRealMatrix(subColumn3);
        assertEquals("Column1", mColumn1,
                m.getColumnMatrix(1));
        assertEquals("Column3", mColumn3,
                m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mColumn3 = new Array2DRowRealMatrix(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertEquals("Row0", mRow0, m.getRowVector(0));
        assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnVector(1));
        assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        double[] mColumn1 = columnToArray(subColumn1);
        double[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m1 = (Array2DRowRealMatrix) m.copy();
        Array2DRowRealMatrix mt = (Array2DRowRealMatrix) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new Array2DRowRealMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testToString
    public void testToString() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        assertEquals("Array2DRowRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
        m = new Array2DRowRealMatrix();
        assertEquals("Array2DRowRealMatrix{}",
                m.toString());
    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        assertEquals(expected, m);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix();
        try {
            m2.setSubMatrix(testData,0,1);
            fail("expecting IllegalStateException");
        } catch (IllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            fail("expecting IllegalStateException");
        } catch (IllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new Array2DRowRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

    }

// org.apache.commons.math.linear.Array2DRowRealMatrixTest::testSerial
    public void testSerial()  {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testConstructors
    public void testConstructors() {

        ArrayRealVector v0 = new ArrayRealVector();
        assertEquals("testData len", 0, v0.getDimension());

        ArrayRealVector v1 = new ArrayRealVector(7);
        assertEquals("testData len", 7, v1.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v1.getEntry(6));

        ArrayRealVector v2 = new ArrayRealVector(5, 1.23);
        assertEquals("testData len", 5, v2.getDimension());
        assertEquals("testData is 1.23 ", 1.23, v2.getEntry(4));

        ArrayRealVector v3 = new ArrayRealVector(vec1);
        assertEquals("testData len", 3, v3.getDimension());
        assertEquals("testData is 2.0 ", 2.0, v3.getEntry(1));

        ArrayRealVector v3_bis = new ArrayRealVector(vec1, true);
        assertEquals("testData len", 3, v3_bis.getDimension());
        assertEquals("testData is 2.0 ", 2.0, v3_bis.getEntry(1));
        assertNotSame(v3_bis.getDataRef(), vec1);
        assertNotSame(v3_bis.getData(), vec1);

        ArrayRealVector v3_ter = new ArrayRealVector(vec1, false);
        assertEquals("testData len", 3, v3_ter.getDimension());
        assertEquals("testData is 2.0 ", 2.0, v3_ter.getEntry(1));
        assertSame(v3_ter.getDataRef(), vec1);
        assertNotSame(v3_ter.getData(), vec1);

        ArrayRealVector v4 = new ArrayRealVector(vec4, 3, 2);
        assertEquals("testData len", 2, v4.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v4.getEntry(0));
        try {
            new ArrayRealVector(vec4, 8, 3);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        RealVector v5_i = new ArrayRealVector(dvec1);
        assertEquals("testData len", 9, v5_i.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v5_i.getEntry(8));

        ArrayRealVector v5 = new ArrayRealVector(dvec1);
        assertEquals("testData len", 9, v5.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v5.getEntry(8));

        ArrayRealVector v6 = new ArrayRealVector(dvec1, 3, 2);
        assertEquals("testData len", 2, v6.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v6.getEntry(0));
        try {
            new ArrayRealVector(dvec1, 8, 3);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v7 = new ArrayRealVector(v1);
        assertEquals("testData len", 7, v7.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v7.getEntry(6));

        RealVectorTestImpl v7_i = new RealVectorTestImpl(vec1);

        ArrayRealVector v7_2 = new ArrayRealVector(v7_i);
        assertEquals("testData len", 3, v7_2.getDimension());
        assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1));

        ArrayRealVector v8 = new ArrayRealVector(v1, true);
        assertEquals("testData len", 7, v8.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6));
        assertNotSame("testData not same object ", v1.data, v8.data);

        ArrayRealVector v8_2 = new ArrayRealVector(v1, false);
        assertEquals("testData len", 7, v8_2.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v8_2.getEntry(6));
        assertEquals("testData same object ", v1.data, v8_2.data);

        ArrayRealVector v9 = new ArrayRealVector(v1, v3);
        assertEquals("testData len", 10, v9.getDimension());
        assertEquals("testData is 1.0 ", 1.0, v9.getEntry(7));

        ArrayRealVector v10 = new ArrayRealVector(v2, new RealVectorTestImpl(vec3));
        assertEquals("testData len", 8, v10.getDimension());
        assertEquals("testData is 1.23 ", 1.23, v10.getEntry(4));
        assertEquals("testData is 7.0 ", 7.0, v10.getEntry(5));

        ArrayRealVector v11 = new ArrayRealVector(new RealVectorTestImpl(vec3), v2);
        assertEquals("testData len", 8, v11.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v11.getEntry(2));
        assertEquals("testData is 1.23 ", 1.23, v11.getEntry(3));

        ArrayRealVector v12 = new ArrayRealVector(v2, vec3);
        assertEquals("testData len", 8, v12.getDimension());
        assertEquals("testData is 1.23 ", 1.23, v12.getEntry(4));
        assertEquals("testData is 7.0 ", 7.0, v12.getEntry(5));

        ArrayRealVector v13 = new ArrayRealVector(vec3, v2);
        assertEquals("testData len", 8, v13.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v13.getEntry(2));
        assertEquals("testData is 1.23 ", 1.23, v13.getEntry(3));

        ArrayRealVector v14 = new ArrayRealVector(vec3, vec4);
        assertEquals("testData len", 12, v14.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v14.getEntry(2));
        assertEquals("testData is 1.0 ", 1.0, v14.getEntry(3));

        try {
            new ArrayRealVector((double[]) null, false);
            fail("expected exception");
        } catch (NullPointerException npe) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        try {
            new ArrayRealVector(new double[0], false);
            fail("expected exception");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

   }

// org.apache.commons.math.linear.ArrayRealVectorTest::testDataInOut
    public void testDataInOut() {

        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v2 = new ArrayRealVector(vec2);
        ArrayRealVector v4 = new ArrayRealVector(vec4);
        RealVectorTestImpl v2_t = new RealVectorTestImpl(vec2);

        RealVector v_append_1 = v1.append(v2);
        assertEquals("testData len", 6, v_append_1.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v_append_1.getEntry(3));

        RealVector v_append_2 = v1.append(2.0);
        assertEquals("testData len", 4, v_append_2.getDimension());
        assertEquals("testData is 2.0 ", 2.0, v_append_2.getEntry(3));

        RealVector v_append_3 = v1.append(vec2);
        assertEquals("testData len", 6, v_append_3.getDimension());
        assertEquals("testData is  ", 4.0, v_append_3.getEntry(3));

        RealVector v_append_4 = v1.append(v2_t);
        assertEquals("testData len", 6, v_append_4.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v_append_4.getEntry(3));

        RealVector v_append_5 = v1.append((RealVector) v2);
        assertEquals("testData len", 6, v_append_5.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v_append_5.getEntry(3));

        RealVector v_copy = v1.copy();
        assertEquals("testData len", 3, v_copy.getDimension());
        assertNotSame("testData not same object ", v1.data, v_copy.getData());

        double[] a_double = v1.toArray();
        assertEquals("testData len", 3, a_double.length);
        assertNotSame("testData not same object ", v1.data, a_double);

        RealVector vout5 = v4.getSubVector(3, 3);
        assertEquals("testData len", 3, vout5.getDimension());
        assertEquals("testData is 4.0 ", 5.0, vout5.getEntry(1));
        try {
            v4.getSubVector(3, 7);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_set1 = (ArrayRealVector) v1.copy();
        v_set1.setEntry(1, 11.0);
        assertEquals("testData is 11.0 ", 11.0, v_set1.getEntry(1));
        try {
            v_set1.setEntry(3, 11.0);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_set2 = (ArrayRealVector) v4.copy();
        v_set2.set(3, v1);
        assertEquals("testData is 1.0 ", 1.0, v_set2.getEntry(3));
        assertEquals("testData is 7.0 ", 7.0, v_set2.getEntry(6));
        try {
            v_set2.set(7, v1);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_set3 = (ArrayRealVector) v1.copy();
        v_set3.set(13.0);
        assertEquals("testData is 13.0 ", 13.0, v_set3.getEntry(2));

        try {
            v_set3.getEntry(23);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_set4 = (ArrayRealVector) v4.copy();
        v_set4.setSubVector(3, v2_t);
        assertEquals("testData is 1.0 ", 4.0, v_set4.getEntry(3));
        assertEquals("testData is 7.0 ", 7.0, v_set4.getEntry(6));
        try {
            v_set4.setSubVector(7, v2_t);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector vout10 = (ArrayRealVector) v1.copy();
        ArrayRealVector vout10_2 = (ArrayRealVector) v1.copy();
        assertEquals(vout10, vout10_2);
        vout10_2.setEntry(0, 1.1);
        assertNotSame(vout10, vout10_2);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testMapFunctions
    public void testMapFunctions() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);

        
        RealVector v_mapAdd = v1.mapAdd(2.0d);
        double[] result_mapAdd = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAdd,v_mapAdd.getData(),normTolerance);

        
        RealVector v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(2.0d);
        double[] result_mapAddToSelf = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData(),normTolerance);

        
        RealVector v_mapSubtract = v1.mapSubtract(2.0d);
        double[] result_mapSubtract = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtract,v_mapSubtract.getData(),normTolerance);

        
        RealVector v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(2.0d);
        double[] result_mapSubtractToSelf = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData(),normTolerance);

        
        RealVector v_mapMultiply = v1.mapMultiply(2.0d);
        double[] result_mapMultiply = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiply,v_mapMultiply.getData(),normTolerance);

        
        RealVector v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(2.0d);
        double[] result_mapMultiplyToSelf = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData(),normTolerance);

        
        RealVector v_mapDivide = v1.mapDivide(2.0d);
        double[] result_mapDivide = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivide,v_mapDivide.getData(),normTolerance);

        
        RealVector v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(2.0d);
        double[] result_mapDivideToSelf = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData(),normTolerance);

        
        RealVector v_mapPow = v1.mapPow(2.0d);
        double[] result_mapPow = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPow,v_mapPow.getData(),normTolerance);

        
        RealVector v_mapPowToSelf = v1.copy();
        v_mapPowToSelf.mapPowToSelf(2.0d);
        double[] result_mapPowToSelf = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPowToSelf,v_mapPowToSelf.getData(),normTolerance);

        
        RealVector v_mapExp = v1.mapExp();
        double[] result_mapExp = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExp,v_mapExp.getData(),normTolerance);

        
        RealVector v_mapExpToSelf = v1.copy();
        v_mapExpToSelf.mapExpToSelf();
        double[] result_mapExpToSelf = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExpToSelf,v_mapExpToSelf.getData(),normTolerance);

        
        RealVector v_mapExpm1 = v1.mapExpm1();
        double[] result_mapExpm1 = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1,v_mapExpm1.getData(),normTolerance);

        
        RealVector v_mapExpm1ToSelf = v1.copy();
        v_mapExpm1ToSelf.mapExpm1ToSelf();
        double[] result_mapExpm1ToSelf = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1ToSelf,v_mapExpm1ToSelf.getData(),normTolerance);

        
        RealVector v_mapLog = v1.mapLog();
        double[] result_mapLog = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLog,v_mapLog.getData(),normTolerance);

        
        RealVector v_mapLogToSelf = v1.copy();
        v_mapLogToSelf.mapLogToSelf();
        double[] result_mapLogToSelf = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLogToSelf,v_mapLogToSelf.getData(),normTolerance);

        
        RealVector v_mapLog10 = v1.mapLog10();
        double[] result_mapLog10 = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10,v_mapLog10.getData(),normTolerance);

        
        RealVector v_mapLog10ToSelf = v1.copy();
        v_mapLog10ToSelf.mapLog10ToSelf();
        double[] result_mapLog10ToSelf = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10ToSelf,v_mapLog10ToSelf.getData(),normTolerance);

        
        RealVector v_mapLog1p = v1.mapLog1p();
        double[] result_mapLog1p = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1p,v_mapLog1p.getData(),normTolerance);

        
        RealVector v_mapLog1pToSelf = v1.copy();
        v_mapLog1pToSelf.mapLog1pToSelf();
        double[] result_mapLog1pToSelf = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1pToSelf,v_mapLog1pToSelf.getData(),normTolerance);

        
        RealVector v_mapCosh = v1.mapCosh();
        double[] result_mapCosh = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCosh,v_mapCosh.getData(),normTolerance);

        
        RealVector v_mapCoshToSelf = v1.copy();
        v_mapCoshToSelf.mapCoshToSelf();
        double[] result_mapCoshToSelf = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCoshToSelf,v_mapCoshToSelf.getData(),normTolerance);

        
        RealVector v_mapSinh = v1.mapSinh();
        double[] result_mapSinh = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinh,v_mapSinh.getData(),normTolerance);

        
        RealVector v_mapSinhToSelf = v1.copy();
        v_mapSinhToSelf.mapSinhToSelf();
        double[] result_mapSinhToSelf = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinhToSelf,v_mapSinhToSelf.getData(),normTolerance);

        
        RealVector v_mapTanh = v1.mapTanh();
        double[] result_mapTanh = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanh,v_mapTanh.getData(),normTolerance);

        
        RealVector v_mapTanhToSelf = v1.copy();
        v_mapTanhToSelf.mapTanhToSelf();
        double[] result_mapTanhToSelf = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanhToSelf,v_mapTanhToSelf.getData(),normTolerance);

        
        RealVector v_mapCos = v1.mapCos();
        double[] result_mapCos = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCos,v_mapCos.getData(),normTolerance);

        
        RealVector v_mapCosToSelf = v1.copy();
        v_mapCosToSelf.mapCosToSelf();
        double[] result_mapCosToSelf = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCosToSelf,v_mapCosToSelf.getData(),normTolerance);

        
        RealVector v_mapSin = v1.mapSin();
        double[] result_mapSin = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSin,v_mapSin.getData(),normTolerance);

        
        RealVector v_mapSinToSelf = v1.copy();
        v_mapSinToSelf.mapSinToSelf();
        double[] result_mapSinToSelf = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSinToSelf,v_mapSinToSelf.getData(),normTolerance);

        
        RealVector v_mapTan = v1.mapTan();
        double[] result_mapTan = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTan,v_mapTan.getData(),normTolerance);

        
        RealVector v_mapTanToSelf = v1.copy();
        v_mapTanToSelf.mapTanToSelf();
        double[] result_mapTanToSelf = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTanToSelf,v_mapTanToSelf.getData(),normTolerance);

        double[] vat_a = {0d, 0.5d, 1.0d};
        ArrayRealVector vat = new ArrayRealVector(vat_a);

        
        RealVector v_mapAcos = vat.mapAcos();
        double[] result_mapAcos = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcos,v_mapAcos.getData(),normTolerance);

        
        RealVector v_mapAcosToSelf = vat.copy();
        v_mapAcosToSelf.mapAcosToSelf();
        double[] result_mapAcosToSelf = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcosToSelf,v_mapAcosToSelf.getData(),normTolerance);

        
        RealVector v_mapAsin = vat.mapAsin();
        double[] result_mapAsin = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsin,v_mapAsin.getData(),normTolerance);

        
        RealVector v_mapAsinToSelf = vat.copy();
        v_mapAsinToSelf.mapAsinToSelf();
        double[] result_mapAsinToSelf = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsinToSelf,v_mapAsinToSelf.getData(),normTolerance);

        
        RealVector v_mapAtan = vat.mapAtan();
        double[] result_mapAtan = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtan,v_mapAtan.getData(),normTolerance);

        
        RealVector v_mapAtanToSelf = vat.copy();
        v_mapAtanToSelf.mapAtanToSelf();
        double[] result_mapAtanToSelf = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtanToSelf,v_mapAtanToSelf.getData(),normTolerance);

        
        RealVector v_mapInv = v1.mapInv();
        double[] result_mapInv = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInv,v_mapInv.getData(),normTolerance);

        
        RealVector v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapInvToSelf();
        double[] result_mapInvToSelf = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData(),normTolerance);

        double[] abs_a = {-1.0d, 0.0d, 1.0d};
        ArrayRealVector abs_v = new ArrayRealVector(abs_a);

        
        RealVector v_mapAbs = abs_v.mapAbs();
        double[] result_mapAbs = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbs,v_mapAbs.getData(),normTolerance);

        
        RealVector v_mapAbsToSelf = abs_v.copy();
        v_mapAbsToSelf.mapAbsToSelf();
        double[] result_mapAbsToSelf = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbsToSelf,v_mapAbsToSelf.getData(),normTolerance);

        
        RealVector v_mapSqrt = v1.mapSqrt();
        double[] result_mapSqrt = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrt,v_mapSqrt.getData(),normTolerance);

        
        RealVector v_mapSqrtToSelf = v1.copy();
        v_mapSqrtToSelf.mapSqrtToSelf();
        double[] result_mapSqrtToSelf = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrtToSelf,v_mapSqrtToSelf.getData(),normTolerance);

        double[] cbrt_a = {-2.0d, 0.0d, 2.0d};
        ArrayRealVector cbrt_v = new ArrayRealVector(cbrt_a);

        
        RealVector v_mapCbrt = cbrt_v.mapCbrt();
        double[] result_mapCbrt = {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrt,v_mapCbrt.getData(),normTolerance);

        
        RealVector v_mapCbrtToSelf = cbrt_v.copy();
        v_mapCbrtToSelf.mapCbrtToSelf();
        double[] result_mapCbrtToSelf =  {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrtToSelf,v_mapCbrtToSelf.getData(),normTolerance);

        double[] ceil_a = {-1.1d, 0.9d, 1.1d};
        ArrayRealVector ceil_v = new ArrayRealVector(ceil_a);

        
        RealVector v_mapCeil = ceil_v.mapCeil();
        double[] result_mapCeil = {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeil,v_mapCeil.getData(),normTolerance);

        
        RealVector v_mapCeilToSelf = ceil_v.copy();
        v_mapCeilToSelf.mapCeilToSelf();
        double[] result_mapCeilToSelf =  {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeilToSelf,v_mapCeilToSelf.getData(),normTolerance);

        
        RealVector v_mapFloor = ceil_v.mapFloor();
        double[] result_mapFloor = {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloor,v_mapFloor.getData(),normTolerance);

        
        RealVector v_mapFloorToSelf = ceil_v.copy();
        v_mapFloorToSelf.mapFloorToSelf();
        double[] result_mapFloorToSelf =  {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloorToSelf,v_mapFloorToSelf.getData(),normTolerance);

        
        RealVector v_mapRint = ceil_v.mapRint();
        double[] result_mapRint = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRint,v_mapRint.getData(),normTolerance);

        
        RealVector v_mapRintToSelf = ceil_v.copy();
        v_mapRintToSelf.mapRintToSelf();
        double[] result_mapRintToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRintToSelf,v_mapRintToSelf.getData(),normTolerance);

        
        RealVector v_mapSignum = ceil_v.mapSignum();
        double[] result_mapSignum = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignum,v_mapSignum.getData(),normTolerance);

        
        RealVector v_mapSignumToSelf = ceil_v.copy();
        v_mapSignumToSelf.mapSignumToSelf();
        double[] result_mapSignumToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignumToSelf,v_mapSignumToSelf.getData(),normTolerance);

        
        
        RealVector v_mapUlp = ceil_v.mapUlp();
        double[] result_mapUlp = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlp,v_mapUlp.getData(),normTolerance);

        
        RealVector v_mapUlpToSelf = ceil_v.copy();
        v_mapUlpToSelf.mapUlpToSelf();
        double[] result_mapUlpToSelf = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlpToSelf,v_mapUlpToSelf.getData(),normTolerance);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testBasicFunctions
    public void testBasicFunctions() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v2 = new ArrayRealVector(vec2);
        ArrayRealVector v5 = new ArrayRealVector(vec5);
        ArrayRealVector v_null = new ArrayRealVector(vec_null);

        RealVectorTestImpl v2_t = new RealVectorTestImpl(vec2);

        
        double d_getNorm = v5.getNorm();
        assertEquals("compare values  ", 8.4261497731763586307, d_getNorm);

        
        double d_getL1Norm = v5.getL1Norm();
        assertEquals("compare values  ", 17.0, d_getL1Norm);

        
        double d_getLInfNorm = v5.getLInfNorm();
        assertEquals("compare values  ", 6.0, d_getLInfNorm);

        
        double dist = v1.getDistance(v2);
        assertEquals("compare values  ",v1.subtract(v2).getNorm(), dist );

        
        double dist_2 = v1.getDistance(v2_t);
        assertEquals("compare values  ", v1.subtract(v2).getNorm(),dist_2 );

        
        double dist_3 = v1.getDistance((RealVector) v2);
        assertEquals("compare values  ", v1.subtract(v2).getNorm(),dist_3 );

        
        double d_getL1Distance = v1. getL1Distance(v2);
        assertEquals("compare values  ",9d, d_getL1Distance );

        double d_getL1Distance_2 = v1. getL1Distance(v2_t);
        assertEquals("compare values  ",9d, d_getL1Distance_2 );

        double d_getL1Distance_3 = v1. getL1Distance((RealVector) v2);
        assertEquals("compare values  ",9d, d_getL1Distance_3 );

        
        double d_getLInfDistance = v1. getLInfDistance(v2);
        assertEquals("compare values  ",3d, d_getLInfDistance );

        double d_getLInfDistance_2 = v1. getLInfDistance(v2_t);
        assertEquals("compare values  ",3d, d_getLInfDistance_2 );

        double d_getLInfDistance_3 = v1. getLInfDistance((RealVector) v2);
        assertEquals("compare values  ",3d, d_getLInfDistance_3 );

        
        ArrayRealVector v_add = v1.add(v2);
        double[] result_add = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add.getData(),result_add,normTolerance);

        RealVectorTestImpl vt2 = new RealVectorTestImpl(vec2);
        RealVector v_add_i = v1.add(vt2);
        double[] result_add_i = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add_i.getData(),result_add_i,normTolerance);

        
        ArrayRealVector v_subtract = v1.subtract(v2);
        double[] result_subtract = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract.getData(),result_subtract,normTolerance);

        RealVector v_subtract_i = v1.subtract(vt2);
        double[] result_subtract_i = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract_i.getData(),result_subtract_i,normTolerance);

        
        ArrayRealVector  v_ebeMultiply = v1.ebeMultiply(v2);
        double[] result_ebeMultiply = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply,normTolerance);

        RealVector  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        double[] result_ebeMultiply_2 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2,normTolerance);

        RealVector  v_ebeMultiply_3 = v1.ebeMultiply((RealVector) v2);
        double[] result_ebeMultiply_3 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_3.getData(),result_ebeMultiply_3,normTolerance);

        
        ArrayRealVector  v_ebeDivide = v1.ebeDivide(v2);
        double[] result_ebeDivide = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide.getData(),result_ebeDivide,normTolerance);

        RealVector  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        double[] result_ebeDivide_2 = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2,normTolerance);

        RealVector  v_ebeDivide_3 = v1.ebeDivide((RealVector) v2);
        double[] result_ebeDivide_3 = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide_3.getData(),result_ebeDivide_3,normTolerance);

        
        double dot =  v1.dotProduct(v2);
        assertEquals("compare val ",32d, dot);

        
        double dot_2 =  v1.dotProduct(v2_t);
        assertEquals("compare val ",32d, dot_2);

        RealMatrix m_outerProduct = v1.outerProduct(v2);
        assertEquals("compare val ",4d, m_outerProduct.getEntry(0,0));

        RealMatrix m_outerProduct_2 = v1.outerProduct(v2_t);
        assertEquals("compare val ",4d, m_outerProduct_2.getEntry(0,0));

        RealMatrix m_outerProduct_3 = v1.outerProduct((RealVector) v2);
        assertEquals("compare val ",4d, m_outerProduct_3.getEntry(0,0));

        RealVector v_unitVector = v1.unitVector();
        RealVector v_unitVector_2 = v1.mapDivide(v1.getNorm());
        assertClose("compare vect" ,v_unitVector.getData(),v_unitVector_2.getData(),normTolerance);

        try {
            v_null.unitVector();
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_unitize = (ArrayRealVector)v1.copy();
        v_unitize.unitize();
        assertClose("compare vect" ,v_unitVector_2.getData(),v_unitize.getData(),normTolerance);
        try {
            v_null.unitize();
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        ArrayRealVector v_projection = v1.projection(v2);
        double[] result_projection = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection.getData(), result_projection, normTolerance);

        RealVector v_projection_2 = v1.projection(v2_t);
        double[] result_projection_2 = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection_2.getData(), result_projection_2, normTolerance);

        RealVector v_projection_3 = v1.projection(v2.getData());
        double[] result_projection_3 = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection_3.getData(), result_projection_3, normTolerance);

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testMisc
    public void testMisc() {
        ArrayRealVector v1 = new ArrayRealVector(vec1);
        ArrayRealVector v4 = new ArrayRealVector(vec4);
        RealVector v4_2 = new ArrayRealVector(vec4);

        String out1 = v1.toString();
        assertTrue("some output ",  out1.length()!=0);
        
        try {
            v1.checkVectorDimensions(2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

       try {
            v1.checkVectorDimensions(v4);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        try {
            v1.checkVectorDimensions(v4_2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testPredicates
    public void testPredicates() {

        ArrayRealVector v = new ArrayRealVector(new double[] { 0, 1, 2 });

        assertFalse(v.isNaN());
        v.setEntry(1, Double.NaN);
        assertTrue(v.isNaN());

        assertFalse(v.isInfinite());
        v.setEntry(0, Double.POSITIVE_INFINITY);
        assertFalse(v.isInfinite());
        v.setEntry(1, 1);
        assertTrue(v.isInfinite());
        v.setEntry(0, 1);
        assertFalse(v.isInfinite());

        v.setEntry(0, 0);
        assertEquals(v, new ArrayRealVector(new double[] { 0, 1, 2 }));
        assertNotSame(v, new ArrayRealVector(new double[] { 0, 1, 2 + Math.ulp(2)}));
        assertNotSame(v, new ArrayRealVector(new double[] { 0, 1, 2, 3 }));

        assertEquals(new ArrayRealVector(new double[] { Double.NaN, 1, 2 }).hashCode(),
                     new ArrayRealVector(new double[] { 0, Double.NaN, 2 }).hashCode());

        assertTrue(new ArrayRealVector(new double[] { Double.NaN, 1, 2 }).hashCode() !=
                   new ArrayRealVector(new double[] { 0, 1, 2 }).hashCode());

        assertTrue(v.equals(v));
        assertTrue(v.equals(v.copy()));
        assertFalse(v.equals(null));
        assertFalse(v.equals(v.getDataRef()));
        assertFalse(v.equals(v.getSubVector(0, v.getDimension() - 1)));
        assertTrue(v.equals(v.getSubVector(0, v.getDimension())));

    }

// org.apache.commons.math.linear.ArrayRealVectorTest::testSerial
    public void testSerial()  {
        ArrayRealVector v = new ArrayRealVector(new double[] { 0, 1, 2 });
        assertEquals(v,TestUtils.serializeAndRecover(v));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testDimensions
    public void testDimensions() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Random r = new Random(66636328996002l);
        BlockRealMatrix m1 = createRandomMatrix(r, 47, 83);
        BlockRealMatrix m2 = new BlockRealMatrix(m1.getData());
        assertEquals(m1, m2);
        BlockRealMatrix m3 = new BlockRealMatrix(testData);
        BlockRealMatrix m4 = new BlockRealMatrix(m3.getData());
        assertEquals(m3, m4);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testAdd
    public void testAdd() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix mInv = new BlockRealMatrix(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
                    testDataPlusInv[row][col],sumEntries[row][col],
                        entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testAddFail
    public void testAddFail() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testNorm
    public void testNorm() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertEquals("testData Frobenius norm", Math.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        assertEquals("testData2 Frobenius norm", Math.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m2 = new BlockRealMatrix(testDataInv);
        assertClose(m.subtract(m2), m2.scalarMultiply(-1d).add(m), entryTolerance);
        try {
            m.subtract(new BlockRealMatrix(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testMultiply
     public void testMultiply() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix mInv = new BlockRealMatrix(testDataInv);
        BlockRealMatrix identity = new BlockRealMatrix(id);
        BlockRealMatrix m2 = new BlockRealMatrix(testData2);
        assertClose(m.multiply(mInv), identity, entryTolerance);
        assertClose(mInv.multiply(m), identity, entryTolerance);
        assertClose(m.multiply(identity), m, entryTolerance);
        assertClose(identity.multiply(mInv), mInv, entryTolerance);
        assertClose(m2.multiply(identity), m2, entryTolerance);
        try {
            m.multiply(new BlockRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSeveralBlocks
    public void testSeveralBlocks() {

        RealMatrix m = new BlockRealMatrix(35, 71);
        for (int i = 0; i < m.getRowDimension(); ++i) {
            for (int j = 0; j < m.getColumnDimension(); ++j) {
                m.setEntry(i, j, i + j / 1024.0);
            }
        }

        RealMatrix mT = m.transpose();
        assertEquals(m.getRowDimension(), mT.getColumnDimension());
        assertEquals(m.getColumnDimension(), mT.getRowDimension());
        for (int i = 0; i < mT.getRowDimension(); ++i) {
            for (int j = 0; j < mT.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(j, i), mT.getEntry(i, j), 0);
            }
        }

        RealMatrix mPm = m.add(m);
        for (int i = 0; i < mPm.getRowDimension(); ++i) {
            for (int j = 0; j < mPm.getColumnDimension(); ++j) {
                assertEquals(2 * m.getEntry(i, j), mPm.getEntry(i, j), 0);
            }
        }

        RealMatrix mPmMm = mPm.subtract(m);
        for (int i = 0; i < mPmMm.getRowDimension(); ++i) {
            for (int j = 0; j < mPmMm.getColumnDimension(); ++j) {
                assertEquals(m.getEntry(i, j), mPmMm.getEntry(i, j), 0);
            }
        }

        RealMatrix mTm = mT.multiply(m);
        for (int i = 0; i < mTm.getRowDimension(); ++i) {
            for (int j = 0; j < mTm.getColumnDimension(); ++j) {
                double sum = 0;
                for (int k = 0; k < mT.getColumnDimension(); ++k) {
                    sum += (k + i / 1024.0) * (k + j / 1024.0);
                }
                assertEquals(sum, mTm.getEntry(i, j), 0);
            }
        }

        RealMatrix mmT = m.multiply(mT);
        for (int i = 0; i < mmT.getRowDimension(); ++i) {
            for (int j = 0; j < mmT.getColumnDimension(); ++j) {
                double sum = 0;
                for (int k = 0; k < m.getColumnDimension(); ++k) {
                    sum += (i + k / 1024.0) * (j + k / 1024.0);
                }
                assertEquals(sum, mmT.getEntry(i, j), 0);
            }
        }

        RealMatrix sub1 = m.getSubMatrix(2, 9, 5, 20);
        for (int i = 0; i < sub1.getRowDimension(); ++i) {
            for (int j = 0; j < sub1.getColumnDimension(); ++j) {
                assertEquals((i + 2) + (j + 5) / 1024.0, sub1.getEntry(i, j), 0);
            }
        }

        RealMatrix sub2 = m.getSubMatrix(10, 12, 3, 70);
        for (int i = 0; i < sub2.getRowDimension(); ++i) {
            for (int j = 0; j < sub2.getColumnDimension(); ++j) {
                assertEquals((i + 10) + (j + 3) / 1024.0, sub2.getEntry(i, j), 0);
            }
        }

        RealMatrix sub3 = m.getSubMatrix(30, 34, 0, 5);
        for (int i = 0; i < sub3.getRowDimension(); ++i) {
            for (int j = 0; j < sub3.getColumnDimension(); ++j) {
                assertEquals((i + 30) + (j + 0) / 1024.0, sub3.getEntry(i, j), 0);
            }
        }

        RealMatrix sub4 = m.getSubMatrix(30, 32, 62, 65);
        for (int i = 0; i < sub4.getRowDimension(); ++i) {
            for (int j = 0; j < sub4.getColumnDimension(); ++j) {
                assertEquals((i + 30) + (j + 62) / 1024.0, sub4.getEntry(i, j), 0);
            }
        }

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testMultiply2
    public void testMultiply2() {
       RealMatrix m3 = new BlockRealMatrix(d3);
       RealMatrix m4 = new BlockRealMatrix(d4);
       RealMatrix m5 = new BlockRealMatrix(d5);
       assertClose(m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.BlockRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = new BlockRealMatrix(id);
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new BlockRealMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(new BlockRealMatrix(testDataPlus2), m.scalarAdd(2d), entryTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testOperate
    public void testOperate() {
        RealMatrix m = new BlockRealMatrix(id);
        assertClose(testVector, m.operate(testVector), entryTolerance);
        assertClose(testVector, m.operate(new ArrayRealVector(testVector)).getData(), entryTolerance);
        m = new BlockRealMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testOperateLarge
    public void testOperateLarge() {
        int p = (7 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int q = (5 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int r =  3 * BlockRealMatrix.BLOCK_SIZE;
        Random random = new Random(111007463902334l);
        RealMatrix m1 = createRandomMatrix(random, p, q);
        RealMatrix m2 = createRandomMatrix(random, q, r);
        RealMatrix m1m2 = m1.multiply(m2);
        for (int i = 0; i < r; ++i) {
            checkArrays(m1m2.getColumn(i), m1.operate(m2.getColumn(i)));
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testOperatePremultiplyLarge
    public void testOperatePremultiplyLarge() {
        int p = (7 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int q = (5 * BlockRealMatrix.BLOCK_SIZE) / 2;
        int r =  3 * BlockRealMatrix.BLOCK_SIZE;
        Random random = new Random(111007463902334l);
        RealMatrix m1 = createRandomMatrix(random, p, q);
        RealMatrix m2 = createRandomMatrix(random, q, r);
        RealMatrix m1m2 = m1.multiply(m2);
        for (int i = 0; i < p; ++i) {
            checkArrays(m1m2.getRow(i), m2.preMultiply(m1.getRow(i)));
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = new BlockRealMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        });
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0], 1.0e-12);
        assertEquals( 7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new BlockRealMatrix(testData);
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        assertClose(mIT, mTI, normTolerance);
        m = new BlockRealMatrix(testData2);
        RealMatrix mt = new BlockRealMatrix(testData2T);
        assertClose(mt, m.transpose(), normTolerance);
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(m.preMultiply(testVector), preMultTest, normTolerance);
        assertClose(m.preMultiply(new ArrayRealVector(testVector).getData()),
                    preMultTest, normTolerance);
        m = new BlockRealMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new BlockRealMatrix(d3);
        RealMatrix m4 = new BlockRealMatrix(d4);
        RealMatrix m5 = new BlockRealMatrix(d5);
        assertClose(m4.preMultiply(m3), m5, entryTolerance);

        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix mInv = new BlockRealMatrix(testDataInv);
        BlockRealMatrix identity = new BlockRealMatrix(id);
        assertClose(m.preMultiply(mInv), identity, entryTolerance);
        assertClose(mInv.preMultiply(m), identity, entryTolerance);
        assertClose(m.preMultiply(identity), m, entryTolerance);
        assertClose(identity.preMultiply(mInv), mInv, entryTolerance);
        try {
            m.preMultiply(new BlockRealMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertClose(m.getRow(0), testDataRow1, entryTolerance);
        assertClose(m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new BlockRealMatrix(testData);
        assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new BlockRealMatrix(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new BlockRealMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new BlockRealMatrix(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        checkGetSubMatrix(m, subRows23Cols00,  2 , 3 , 0, 0);
        checkGetSubMatrix(m, subRows00Cols33,  0 , 0 , 3, 3);
        checkGetSubMatrix(m, subRows01Cols23,  0 , 1 , 2, 3);
        checkGetSubMatrix(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 });
        checkGetSubMatrix(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 });
        checkGetSubMatrix(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 });
        checkGetSubMatrix(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 });
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkGetSubMatrix(m, null,  1, 0, 2, 4);
        checkGetSubMatrix(m, null, -1, 1, 2, 2);
        checkGetSubMatrix(m, null,  1, 0, 2, 2);
        checkGetSubMatrix(m, null,  1, 0, 2, 4);
        checkGetSubMatrix(m, null, new int[] {},    new int[] { 0 });
        checkGetSubMatrix(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetMatrixLarge
    public void testGetSetMatrixLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealMatrix sub = new BlockRealMatrix(n - 4, n - 4).scalarAdd(1);

        m.setSubMatrix(sub.getData(), 2, 2);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((i < 2) || (i > n - 3) || (j < 2) || (j > n - 3)) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getSubMatrix(2, n - 3, 2, n - 3));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        checkCopy(m, subRows23Cols00,  2 , 3 , 0, 0);
        checkCopy(m, subRows00Cols33,  0 , 0 , 3, 3);
        checkCopy(m, subRows01Cols23,  0 , 1 , 2, 3);
        checkCopy(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 });
        checkCopy(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 });
        checkCopy(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 });
        checkCopy(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 });
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 });

        checkCopy(m, null,  1, 0, 2, 4);
        checkCopy(m, null, -1, 1, 2, 2);
        checkCopy(m, null,  1, 0, 2, 2);
        checkCopy(m, null,  1, 0, 2, 4);
        checkCopy(m, null, new int[] {},    new int[] { 0 });
        checkCopy(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m     = new BlockRealMatrix(subTestData);
        RealMatrix mRow0 = new BlockRealMatrix(subRow0);
        RealMatrix mRow3 = new BlockRealMatrix(subRow3);
        assertEquals("Row0", mRow0, m.getRowMatrix(0));
        assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mRow3 = new BlockRealMatrix(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetRowMatrixLarge
    public void testGetSetRowMatrixLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealMatrix sub = new BlockRealMatrix(1, n).scalarAdd(1);

        m.setRowMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getRowMatrix(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mColumn1 = new BlockRealMatrix(subColumn1);
        RealMatrix mColumn3 = new BlockRealMatrix(subColumn3);
        assertEquals(mColumn1, m.getColumnMatrix(1));
        assertEquals(mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealMatrix mColumn3 = new BlockRealMatrix(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetColumnMatrixLarge
    public void testGetSetColumnMatrixLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealMatrix sub = new BlockRealMatrix(n, 1).scalarAdd(1);

        m.setColumnMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getColumnMatrix(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertEquals(mRow0, m.getRowVector(0));
        assertEquals(mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetRowVectorLarge
    public void testGetSetRowVectorLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealVector sub = new ArrayRealVector(n, 1.0);

        m.setRowVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getRowVector(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertEquals(mColumn1, m.getColumnVector(1));
        assertEquals(mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetColumnVectorLarge
    public void testGetSetColumnVectorLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        RealVector sub = new ArrayRealVector(n, 1.0);

        m.setColumnVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        assertEquals(sub, m.getColumnVector(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetRowLarge
    public void testGetSetRowLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        double[] sub = new double[n];
        Arrays.fill(sub, 1.0);

        m.setRow(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        checkArrays(sub, m.getRow(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        double[] mColumn1 = columnToArray(subColumn1);
        double[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new BlockRealMatrix(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testGetSetColumnLarge
    public void testGetSetColumnLarge() {
        int n = 3 * BlockRealMatrix.BLOCK_SIZE;
        RealMatrix m = new BlockRealMatrix(n, n);
        double[] sub = new double[n];
        Arrays.fill(sub, 1.0);

        m.setColumn(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    assertEquals(0.0, m.getEntry(i, j), 0.0);
                } else {
                    assertEquals(1.0, m.getEntry(i, j), 0.0);
                }
            }
        }
        checkArrays(sub, m.getColumn(2));

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        BlockRealMatrix m1 = m.copy();
        BlockRealMatrix mt = m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new BlockRealMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testToString
    public void testToString() {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        assertEquals("BlockRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = new BlockRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new BlockRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new BlockRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        assertEquals(expected, m);

        
        BlockRealMatrix matrix = new BlockRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1 , 2}});
        matrix.setSubMatrix(new double[][] {{3, 4}, {5, 6}}, 1, 1);
        expected = new BlockRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 3, 4, 8}, {9, 5 ,6, 2}});
        assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new BlockRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

    }

// org.apache.commons.math.linear.BlockRealMatrixTest::testSerial
    public void testSerial()  {
        BlockRealMatrix m = new BlockRealMatrix(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.CholeskySolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() throws MathException {
        DecompositionSolver solver =
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.CholeskySolverTest::testSolve
    public void testSolve() throws MathException {
        DecompositionSolver solver =
            new CholeskyDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                {   78,  -13,    1 },
                {  414,  -62,   -1 },
                { 1312, -202,  -37 },
                { 2989, -542,  145 },
                { 5510, -1465, 201 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1,  0,  1 },
                { 0,  1,  1 },
                { 2,  1, -4 },
                { 2,  2,  2 },
                { 5, -3,  0 }
        });

        
        assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 1.0e-13);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         new ArrayRealVector(solver.solve(b.getColumn(i))).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         solver.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            assertEquals(0,
                         solver.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

    }

// org.apache.commons.math.linear.CholeskySolverTest::testDeterminant
    public void testDeterminant() throws MathException {
        assertEquals(7290000.0, getDeterminant(MatrixUtils.createRealMatrix(testData)), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension1
    public void testDimension1() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] { { 1.5 } });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(1.5, ed.getRealEigenvalue(0), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension2
    public void testDimension2() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                    { 59.0, 12.0 },
                    { 12.0, 66.0 }
            });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(75.0, ed.getRealEigenvalue(0), 1.0e-15);
        assertEquals(50.0, ed.getRealEigenvalue(1), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension3
    public void testDimension3() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  39632.0, -4824.0, -16560.0 },
                                   {  -4824.0,  8693.0,   7920.0 },
                                   { -16560.0,  7920.0,  17300.0 }
                               });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(50000.0, ed.getRealEigenvalue(0), 3.0e-11);
        assertEquals(12500.0, ed.getRealEigenvalue(1), 3.0e-11);
        assertEquals( 3125.0, ed.getRealEigenvalue(2), 3.0e-11);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension4WithSplit
    public void testDimension4WithSplit() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  0.784, -0.288,  0.000,  0.000 },
                                   { -0.288,  0.616,  0.000,  0.000 },
                                   {  0.000,  0.000,  0.164, -0.048 },
                                   {  0.000,  0.000, -0.048,  0.136 }
                               });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(1.0, ed.getRealEigenvalue(0), 1.0e-15);
        assertEquals(0.4, ed.getRealEigenvalue(1), 1.0e-15);
        assertEquals(0.2, ed.getRealEigenvalue(2), 1.0e-15);
        assertEquals(0.1, ed.getRealEigenvalue(3), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimension4WithoutSplit
    public void testDimension4WithoutSplit() {
        RealMatrix matrix =
            MatrixUtils.createRealMatrix(new double[][] {
                                   {  0.5608, -0.2016,  0.1152, -0.2976 },
                                   { -0.2016,  0.4432, -0.2304,  0.1152 },
                                   {  0.1152, -0.2304,  0.3088, -0.1344 },
                                   { -0.2976,  0.1152, -0.1344,  0.3872 }
                               });
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(1.0, ed.getRealEigenvalue(0), 1.0e-15);
        assertEquals(0.4, ed.getRealEigenvalue(1), 1.0e-15);
        assertEquals(0.2, ed.getRealEigenvalue(2), 1.0e-15);
        assertEquals(0.1, ed.getRealEigenvalue(3), 1.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testMath308
    public void testMath308() {

        double[] mainTridiagonal = {
            22.330154644539597, 46.65485522478641, 17.393672330044705, 54.46687435351116, 80.17800767709437
        };
        double[] secondaryTridiagonal = {
            13.04450406501361, -5.977590941539671, 2.9040909856707517, 7.1570352792841225
        };

        
        
        double[] refEigenValues = {
            82.044413207204002, 53.456697699894512, 52.536278520113882, 18.847969733754262, 14.138204224043099
        };
        RealVector[] refEigenVectors = {
            new ArrayRealVector(new double[] { -0.000462690386766, -0.002118073109055,  0.011530080757413,  0.252322434584915,  0.967572088232592 }),
            new ArrayRealVector(new double[] {  0.314647769490148,  0.750806415553905, -0.167700312025760, -0.537092972407375,  0.143854968127780 }),
            new ArrayRealVector(new double[] {  0.222368839324646,  0.514921891363332, -0.021377019336614,  0.801196801016305, -0.207446991247740 }),
            new ArrayRealVector(new double[] {  0.713933751051495, -0.190582113553930,  0.671410443368332, -0.056056055955050,  0.006541576993581 }),
            new ArrayRealVector(new double[] {  0.584677060845929, -0.367177264979103, -0.721453187784497,  0.052971054621812, -0.005740715188257 })
        };

        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-5);
            assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 2.0e-7);
        }

    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testMathpbx02
    public void testMathpbx02() {

        double[] mainTridiagonal = {
              7484.860960227216, 18405.28129035345, 13855.225609560746,
             10016.708722343366, 559.8117399576674, 6750.190788301587, 
                71.21428769782159
        };
        double[] secondaryTridiagonal = {
             -4175.088570476366,1975.7955858241994,5193.178422374075, 
              1995.286659169179,75.34535882933804,-234.0808002076056
        };

        
        
        double[] refEigenValues = {
                20654.744890306974412,16828.208208485466457,
                6893.155912634994820,6757.083016675340332,
                5887.799885688558788,64.309089923240379,
                57.992628792736340
        };
        RealVector[] refEigenVectors = {
                new ArrayRealVector(new double[] {-0.270356342026904, 0.852811091326997, 0.399639490702077, 0.198794657813990, 0.019739323307666, 0.000106983022327, -0.000001216636321}),
                new ArrayRealVector(new double[] {0.179995273578326,-0.402807848153042,0.701870993525734,0.555058211014888,0.068079148898236,0.000509139115227,-0.000007112235617}),
                new ArrayRealVector(new double[] {-0.399582721284727,-0.056629954519333,-0.514406488522827,0.711168164518580,0.225548081276367,0.125943999652923,-0.004321507456014}),
                new ArrayRealVector(new double[] {0.058515721572821,0.010200130057739,0.063516274916536,-0.090696087449378,-0.017148420432597,0.991318870265707,-0.034707338554096}),
                new ArrayRealVector(new double[] {0.855205995537564,0.327134656629775,-0.265382397060548,0.282690729026706,0.105736068025572,-0.009138126622039,0.000367751821196}),
                new ArrayRealVector(new double[] {-0.002913069901144,-0.005177515777101,0.041906334478672,-0.109315918416258,0.436192305456741,0.026307315639535,0.891797507436344}),
                new ArrayRealVector(new double[] {-0.005738311176435,-0.010207611670378,0.082662420517928,-0.215733886094368,0.861606487840411,-0.025478530652759,-0.451080697503958})
        };

        
        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-3);
            if (refEigenVectors[i].dotProduct(decomposition.getEigenvector(i)) < 0) {
                assertEquals(0, refEigenVectors[i].add(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            } else {
                assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            }
        }

    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testMathpbx03
    public void testMathpbx03() {

        double[] mainTridiagonal = {
            1809.0978259647177,3395.4763425956166,1832.1894584712693,3804.364873592377,
            806.0482458637571,2403.656427234185,28.48691431556015
        };
        double[] secondaryTridiagonal = {
            -656.8932064545833,-469.30804108920734,-1021.7714889369421,
            -1152.540497328983,-939.9765163817368,-12.885877015422391
        };

        
        
        double[] refEigenValues = {
            4603.121913685183245,3691.195818048970978,2743.442955402465032,1657.596442107321764,
            1336.797819095331306,30.129865209677519,17.035352085224986
        };

        RealVector[] refEigenVectors = {
            new ArrayRealVector(new double[] {-0.036249830202337,0.154184732411519,-0.346016328392363,0.867540105133093,-0.294483395433451,0.125854235969548,-0.000354507444044}),
            new ArrayRealVector(new double[] {-0.318654191697157,0.912992309960507,-0.129270874079777,-0.184150038178035,0.096521712579439,-0.070468788536461,0.000247918177736}),
            new ArrayRealVector(new double[] {-0.051394668681147,0.073102235876933,0.173502042943743,-0.188311980310942,-0.327158794289386,0.905206581432676,-0.004296342252659}),
            new ArrayRealVector(new double[] {0.838150199198361,0.193305209055716,-0.457341242126146,-0.166933875895419,0.094512811358535,0.119062381338757,-0.000941755685226}),
            new ArrayRealVector(new double[] {0.438071395458547,0.314969169786246,0.768480630802146,0.227919171600705,-0.193317045298647,-0.170305467485594,0.001677380536009}),
            new ArrayRealVector(new double[] {-0.003726503878741,-0.010091946369146,-0.067152015137611,-0.113798146542187,-0.313123000097908,-0.118940107954918,0.932862311396062}),
            new ArrayRealVector(new double[] {0.009373003194332,0.025570377559400,0.170955836081348,0.291954519805750,0.807824267665706,0.320108347088646,0.360202112392266}),
        };

        
        EigenDecomposition decomposition =
            new EigenDecompositionImpl(mainTridiagonal, secondaryTridiagonal, MathUtils.SAFE_MIN);

        double[] eigenValues = decomposition.getRealEigenvalues();
        for (int i = 0; i < refEigenValues.length; ++i) {
            assertEquals(refEigenValues[i], eigenValues[i], 1.0e-4);
            if (refEigenVectors[i].dotProduct(decomposition.getEigenvector(i)) < 0) {
                assertEquals(0, refEigenVectors[i].add(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            } else {
                assertEquals(0, refEigenVectors[i].subtract(decomposition.getEigenvector(i)).getNorm(), 1.0e-5);
            }
        }

    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testTridiagonal
    public void testTridiagonal() {
        Random r = new Random(4366663527842l);
        double[] ref = new double[30];
        for (int i = 0; i < ref.length; ++i) {
            if (i < 5) {
                ref[i] = 2 * r.nextDouble() - 1;
            } else {
                ref[i] = 0.0001 * r.nextDouble() + 6;
            }
        }
        Arrays.sort(ref);
        TriDiagonalTransformer t =
            new TriDiagonalTransformer(createTestMatrix(r, ref));
        EigenDecomposition ed =
            new EigenDecompositionImpl(t.getMainDiagonalRef(),
                                       t.getSecondaryDiagonalRef(),
                                       MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        assertEquals(ref.length, eigenValues.length);
        for (int i = 0; i < ref.length; ++i) {
            assertEquals(ref[ref.length - i - 1], eigenValues[i], 2.0e-14);
        }

    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDimensions
    public void testDimensions() {
        final int m = matrix.getRowDimension();
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        assertEquals(m, ed.getV().getRowDimension());
        assertEquals(m, ed.getV().getColumnDimension());
        assertEquals(m, ed.getD().getColumnDimension());
        assertEquals(m, ed.getD().getColumnDimension());
        assertEquals(m, ed.getVT().getRowDimension());
        assertEquals(m, ed.getVT().getColumnDimension());
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testEigenvalues
    public void testEigenvalues() {
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        assertEquals(refValues.length, eigenValues.length);
        for (int i = 0; i < refValues.length; ++i) {
            assertEquals(refValues[i], eigenValues[i], 3.0e-15);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testBigMatrix
    public void testBigMatrix() {
        Random r = new Random(17748333525117l);
        double[] bigValues = new double[200];
        for (int i = 0; i < bigValues.length; ++i) {
            bigValues[i] = 2 * r.nextDouble() - 1;
        }
        Arrays.sort(bigValues);
        EigenDecomposition ed =
            new EigenDecompositionImpl(createTestMatrix(r, bigValues), MathUtils.SAFE_MIN);
        double[] eigenValues = ed.getRealEigenvalues();
        assertEquals(bigValues.length, eigenValues.length);
        for (int i = 0; i < bigValues.length; ++i) {
            assertEquals(bigValues[bigValues.length - i - 1], eigenValues[i], 2.0e-14);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testEigenvectors
    public void testEigenvectors() {
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        for (int i = 0; i < matrix.getRowDimension(); ++i) {
            double lambda = ed.getRealEigenvalue(i);
            RealVector v  = ed.getEigenvector(i);
            RealVector mV = matrix.operate(v);
            assertEquals(0, mV.subtract(v.mapMultiplyToSelf(lambda)).getNorm(), 1.0e-13);
        }
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testAEqualVDVt
    public void testAEqualVDVt() {
        EigenDecomposition ed = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN);
        RealMatrix v  = ed.getV();
        RealMatrix d  = ed.getD();
        RealMatrix vT = ed.getVT();
        double norm = v.multiply(d).multiply(vT).subtract(matrix).getNorm();
        assertEquals(0, norm, 6.0e-13);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testVOrthogonal
    public void testVOrthogonal() {
        RealMatrix v = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN).getV();
        RealMatrix vTv = v.transpose().multiply(v);
        RealMatrix id  = MatrixUtils.createRealIdentityMatrix(vTv.getRowDimension());
        assertEquals(0, vTv.subtract(id).getNorm(), 2.0e-13);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDiagonal
    public void testDiagonal() {
        double[] diagonal = new double[] { -3.0, -2.0, 2.0, 5.0 };
        RealMatrix m = createDiagonalMatrix(diagonal, diagonal.length, diagonal.length);
        EigenDecomposition ed = new EigenDecompositionImpl(m, MathUtils.SAFE_MIN);
        assertEquals(diagonal[0], ed.getRealEigenvalue(3), 2.0e-15);
        assertEquals(diagonal[1], ed.getRealEigenvalue(2), 2.0e-15);
        assertEquals(diagonal[2], ed.getRealEigenvalue(1), 2.0e-15);
        assertEquals(diagonal[3], ed.getRealEigenvalue(0), 2.0e-15);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testRepeatedEigenvalue
    public void testRepeatedEigenvalue() {
        RealMatrix repeated = MatrixUtils.createRealMatrix(new double[][] {
                {3,  2,  4},
                {2,  0,  2},
                {4,  2,  3}
        });
        EigenDecomposition ed = new EigenDecompositionImpl(repeated, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {8, -1, -1}), ed, 1E-12);
        checkEigenVector((new double[] {2, 1, 2}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testDistinctEigenvalues
    public void testDistinctEigenvalues() {
        RealMatrix distinct = MatrixUtils.createRealMatrix(new double[][] {
                {3, 1, -4},
                {1, 3, -4},
                {-4, -4, 8}
        });
        EigenDecomposition ed = new EigenDecompositionImpl(distinct, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {2, 0, 12}), ed, 1E-12);
        checkEigenVector((new double[] {1, -1, 0}), ed, 1E-12);
        checkEigenVector((new double[] {1, 1, 1}), ed, 1E-12);
        checkEigenVector((new double[] {-1, -1, 2}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenDecompositionImplTest::testZeroDivide
    public void testZeroDivide() {
        RealMatrix indefinite = MatrixUtils.createRealMatrix(new double [][] {
                { 0.0, 1.0, -1.0 }, 
                { 1.0, 1.0, 0.0 }, 
                { -1.0,0.0, 1.0 }        
        });
        EigenDecomposition ed = new EigenDecompositionImpl(indefinite, MathUtils.SAFE_MIN);
        checkEigenValues((new double[] {2, 1, -1}), ed, 1E-12);
        double isqrt3 = 1/Math.sqrt(3.0);
        checkEigenVector((new double[] {isqrt3,isqrt3,-isqrt3}), ed, 1E-12);
        double isqrt2 = 1/Math.sqrt(2.0);
        checkEigenVector((new double[] {0.0,-isqrt2,-isqrt2}), ed, 1E-12);
        double isqrt6 = 1/Math.sqrt(6.0);
        checkEigenVector((new double[] {2*isqrt6,-isqrt6,isqrt6}), ed, 1E-12);
    }

// org.apache.commons.math.linear.EigenSolverTest::testNonInvertible
    public void testNonInvertible() {
        Random r = new Random(9994100315209l);
        RealMatrix m =
            EigenDecompositionImplTest.createTestMatrix(r, new double[] { 1.0, 0.0, -1.0, -2.0, -3.0 });
        DecompositionSolver es = new EigenDecompositionImpl(m, MathUtils.SAFE_MIN).getSolver();
        assertFalse(es.isNonSingular());
        try {
            es.getInverse();
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.EigenSolverTest::testInvertible
    public void testInvertible() {
        Random r = new Random(9994100315209l);
        RealMatrix m =
            EigenDecompositionImplTest.createTestMatrix(r, new double[] { 1.0, 0.5, -1.0, -2.0, -3.0 });
        DecompositionSolver es = new EigenDecompositionImpl(m, MathUtils.SAFE_MIN).getSolver();
        assertTrue(es.isNonSingular());
        RealMatrix inverse = es.getInverse();
        RealMatrix error =
            m.multiply(inverse).subtract(MatrixUtils.createRealIdentityMatrix(m.getRowDimension()));
        assertEquals(0, error.getNorm(), 4.0e-15);
    }

// org.apache.commons.math.linear.EigenSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver es = new EigenDecompositionImpl(matrix, MathUtils.SAFE_MIN).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            es.solve(b);
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            es.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            es.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.EigenSolverTest::testSolve
    public void testSolve() {
        RealMatrix m = MatrixUtils.createRealMatrix(new double[][] {
                { 91,  5, 29, 32, 40, 14 },
                {  5, 34, -1,  0,  2, -1 },
                { 29, -1, 12,  9, 21,  8 },
                { 32,  0,  9, 14,  9,  0 },
                { 40,  2, 21,  9, 51, 19 },
                { 14, -1,  8,  0, 19, 14 }
        });
        DecompositionSolver es = new EigenDecompositionImpl(m, MathUtils.SAFE_MIN).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { 1561, 269, 188 },
                {   69, -21,  70 },
                {  739, 108,  63 },
                {  324,  86,  59 },
                { 1624, 194, 107 },
                {  796,  69,  36 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1,   2, 1 },
                { 2,  -1, 2 },
                { 4,   2, 3 },
                { 8,  -1, 0 },
                { 16,  2, 0 },
                { 32, -1, 0 }
        });

        
        assertEquals(0, es.solve(b).subtract(xRef).getNorm(), 2.0e-12);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         new ArrayRealVector(es.solve(b.getColumn(i))).subtract(xRef.getColumnVector(i)).getNorm(),
                         2.0e-11);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         es.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         2.0e-11);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            assertEquals(0,
                         es.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         2.0e-11);
        }

    }

// org.apache.commons.math.linear.LUSolverTest::testThreshold
    public void testThreshold() {
        final RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
                                                       { 1.0, 2.0, 3.0},
                                                       { 2.0, 5.0, 3.0},
                                                       { 4.000001, 9.0, 9.0}
                                                     });
        assertFalse(new LUDecompositionImpl(matrix, 1.0e-5).getSolver().isNonSingular());
        assertTrue(new LUDecompositionImpl(matrix, 1.0e-10).getSolver().isNonSingular());
    }

// org.apache.commons.math.linear.LUSolverTest::testSingular
    public void testSingular() {
        DecompositionSolver solver =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        assertTrue(solver.isNonSingular());
        solver = new LUDecompositionImpl(MatrixUtils.createRealMatrix(singular)).getSolver();
        assertFalse(solver.isNonSingular());
        solver = new LUDecompositionImpl(MatrixUtils.createRealMatrix(bigSingular)).getSolver();
        assertFalse(solver.isNonSingular());
    }

// org.apache.commons.math.linear.LUSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.LUSolverTest::testSolveSingularityErrors
    public void testSolveSingularityErrors() {
        DecompositionSolver solver =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(singular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumnVector(0));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.LUSolverTest::testSolve
    public void testSolve() {
        DecompositionSolver solver =
            new LUDecompositionImpl(MatrixUtils.createRealMatrix(testData)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { 1, 0 }, { 2, -5 }, { 3, 1 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 19, -71 }, { -6, 22 }, { -2, 9 }
        });

        
        assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 1.0e-13);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         new ArrayRealVector(solver.solve(b.getColumn(i))).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         solver.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            assertEquals(0,
                         solver.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

    }

// org.apache.commons.math.linear.LUSolverTest::testDeterminant
    public void testDeterminant() {
        assertEquals( -1, getDeterminant(MatrixUtils.createRealMatrix(testData)), 1.0e-15);
        assertEquals(-10, getDeterminant(MatrixUtils.createRealMatrix(luData)), 1.0e-14);
        assertEquals(  0, getDeterminant(MatrixUtils.createRealMatrix(singular)), 1.0e-17);
        assertEquals(  0, getDeterminant(MatrixUtils.createRealMatrix(bigSingular)), 1.0e-10);
    }

// org.apache.commons.math.linear.QRSolverTest::testRank
    public void testRank() {
        DecompositionSolver solver =
            new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3NonSingular)).getSolver();
        assertTrue(solver.isNonSingular());

        solver = new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3Singular)).getSolver();
        assertFalse(solver.isNonSingular());

        solver = new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x4)).getSolver();
        assertTrue(solver.isNonSingular());

        solver = new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData4x3)).getSolver();
        assertTrue(solver.isNonSingular());

    }

// org.apache.commons.math.linear.QRSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3NonSingular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumnVector(0));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.QRSolverTest::testSolveRankErrors
    public void testSolveRankErrors() {
        DecompositionSolver solver =
            new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3Singular)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[3][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumnVector(0));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.QRSolverTest::testSolve
    public void testSolve() {
        QRDecomposition decomposition =
            new QRDecompositionImpl(MatrixUtils.createRealMatrix(testData3x3NonSingular));
        DecompositionSolver solver = decomposition.getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { -102, 12250 }, { 544, 24500 }, { 167, -36750 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { 1, 2515 }, { 2, 422 }, { -3, 898 }
        });

        
        assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), 2.0e-16 * xRef.getNorm());

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            final double[] x = solver.solve(b.getColumn(i));
            final double error = new ArrayRealVector(x).subtract(xRef.getColumnVector(i)).getNorm();
            assertEquals(0, error, 3.0e-16 * xRef.getColumnVector(i).getNorm());
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            final RealVector x = solver.solve(b.getColumnVector(i));
            final double error = x.subtract(xRef.getColumnVector(i)).getNorm();
            assertEquals(0, error, 3.0e-16 * xRef.getColumnVector(i).getNorm());
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            final RealVector x = solver.solve(v);
            final double error = x.subtract(xRef.getColumnVector(i)).getNorm();
            assertEquals(0, error, 3.0e-16 * xRef.getColumnVector(i).getNorm());
        }

    }

// org.apache.commons.math.linear.QRSolverTest::testOverdetermined
    public void testOverdetermined() {
        final Random r    = new Random(5559252868205245l);
        int          p    = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int          q    = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        RealMatrix   a    = createTestMatrix(r, p, q);
        RealMatrix   xRef = createTestMatrix(r, q, BlockRealMatrix.BLOCK_SIZE + 3);

        
        RealMatrix b = a.multiply(xRef);
        final double noise = 0.001;
        b.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value) {
                return value * (1.0 + noise * (2 * r.nextDouble() - 1));
            }
        });

        
        RealMatrix x = new QRDecompositionImpl(a).getSolver().solve(b);
        assertEquals(0, x.subtract(xRef).getNorm(), 0.01 * noise * p * q);

    }

// org.apache.commons.math.linear.QRSolverTest::testUnderdetermined
    public void testUnderdetermined() {
        final Random r    = new Random(42185006424567123l);
        int          p    = (5 * BlockRealMatrix.BLOCK_SIZE) / 4;
        int          q    = (7 * BlockRealMatrix.BLOCK_SIZE) / 4;
        RealMatrix   a    = createTestMatrix(r, p, q);
        RealMatrix   xRef = createTestMatrix(r, q, BlockRealMatrix.BLOCK_SIZE + 3);
        RealMatrix   b    = a.multiply(xRef);
        RealMatrix   x = new QRDecompositionImpl(a).getSolver().solve(b);

        
        assertTrue(x.subtract(xRef).getNorm() / (p * q) > 0.01);

        
        assertEquals(0.0, x.getSubMatrix(p, q - 1, 0, x.getColumnDimension() - 1).getNorm());

    }

// org.apache.commons.math.linear.RealMatrixImplTest::testDimensions
    public void testDimensions() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testCopyFunctions
    public void testCopyFunctions() {
        RealMatrixImpl m1 = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(m1.getData());
        assertEquals(m2,m1);
        RealMatrixImpl m3 = new RealMatrixImpl(testData);
        RealMatrixImpl m4 = new RealMatrixImpl(m3.getData(), false);
        assertEquals(m4,m3);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testAdd
    public void testAdd() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
                    testDataPlusInv[row][col],sumEntries[row][col],
                        entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testAddFail
    public void testAddFail() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testNorm
    public void testNorm() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertEquals("testData Frobenius norm", Math.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        assertEquals("testData2 Frobenius norm", Math.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPlusMinus
    public void testPlusMinus() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testDataInv);
        TestUtils.assertEquals("m-n = m + -n",m.subtract(m2),
            m2.scalarMultiply(-1d).add(m),entryTolerance);
        try {
            m.subtract(new RealMatrixImpl(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMultiply
     public void testMultiply() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
        RealMatrixImpl identity = new RealMatrixImpl(id);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        TestUtils.assertEquals("inverse multiply",m.multiply(mInv),
            identity,entryTolerance);
        TestUtils.assertEquals("inverse multiply",mInv.multiply(m),
            identity,entryTolerance);
        TestUtils.assertEquals("identity multiply",m.multiply(identity),
            m,entryTolerance);
        TestUtils.assertEquals("identity multiply",identity.multiply(mInv),
            mInv,entryTolerance);
        TestUtils.assertEquals("identity multiply",m2.multiply(identity),
            m2,entryTolerance);
        try {
            m.multiply(new RealMatrixImpl(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMultiply2
    public void testMultiply2() {
       RealMatrix m3 = new RealMatrixImpl(d3);
       RealMatrix m4 = new RealMatrixImpl(d4);
       RealMatrix m5 = new RealMatrixImpl(d5);
       TestUtils.assertEquals("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.RealMatrixImplTest::testTrace
    public void testTrace() {
        RealMatrix m = new RealMatrixImpl(id);
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new RealMatrixImpl(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new RealMatrixImpl(testData);
        TestUtils.assertEquals("scalar add",new RealMatrixImpl(testDataPlus2),
            m.scalarAdd(2d),entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testOperate
    public void testOperate() {
        RealMatrix m = new RealMatrixImpl(id);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(testVector), entryTolerance);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(new ArrayRealVector(testVector)).getData(), entryTolerance);
        m = new RealMatrixImpl(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMath209
    public void testMath209() {
        RealMatrix a = new RealMatrixImpl(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        }, false);
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0], 1.0e-12);
        assertEquals( 7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new RealMatrixImpl(testData);
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals("inverse-transpose", mIT, mTI, normTolerance);
        m = new RealMatrixImpl(testData2);
        RealMatrix mt = new RealMatrixImpl(testData2T);
        TestUtils.assertEquals("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new RealMatrixImpl(testData);
        TestUtils.assertEquals("premultiply", m.preMultiply(testVector),
                    preMultTest, normTolerance);
        TestUtils.assertEquals("premultiply", m.preMultiply(new ArrayRealVector(testVector).getData()),
                    preMultTest, normTolerance);
        m = new RealMatrixImpl(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new RealMatrixImpl(d3);
        RealMatrix m4 = new RealMatrixImpl(d4);
        RealMatrix m5 = new RealMatrixImpl(d5);
        TestUtils.assertEquals("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
        RealMatrixImpl identity = new RealMatrixImpl(id);
        TestUtils.assertEquals("inverse multiply",m.preMultiply(mInv),
                identity,entryTolerance);
        TestUtils.assertEquals("inverse multiply",mInv.preMultiply(m),
                identity,entryTolerance);
        TestUtils.assertEquals("identity multiply",m.preMultiply(identity),
                m,entryTolerance);
        TestUtils.assertEquals("identity multiply",identity.preMultiply(mInv),
                mInv,entryTolerance);
        try {
            m.preMultiply(new RealMatrixImpl(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new RealMatrixImpl(testData);
        TestUtils.assertEquals("get row",m.getRow(0),testDataRow1,entryTolerance);
        TestUtils.assertEquals("get col",m.getColumn(2),testDataCol3,entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new RealMatrixImpl(testData);
        assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            fail ("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new RealMatrixImpl(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new RealMatrixImpl(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new RealMatrixImpl(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);

    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetSubMatrix
    public void testGetSubMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        checkGetSubMatrix(m, subRows23Cols00,  2 , 3 , 0, 0, false);
        checkGetSubMatrix(m, subRows00Cols33,  0 , 0 , 3, 3, false);
        checkGetSubMatrix(m, subRows01Cols23,  0 , 1 , 2, 3, false);
        checkGetSubMatrix(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 },    false);
        checkGetSubMatrix(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 },    false);
        checkGetSubMatrix(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 }, false);
        checkGetSubMatrix(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 }, false);
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkGetSubMatrix(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkGetSubMatrix(m, null,  1, 0, 2, 4, true);
        checkGetSubMatrix(m, null, -1, 1, 2, 2, true);
        checkGetSubMatrix(m, null,  1, 0, 2, 2, true);
        checkGetSubMatrix(m, null,  1, 0, 2, 4, true);
        checkGetSubMatrix(m, null, new int[] {},    new int[] { 0 }, true);
        checkGetSubMatrix(m, null, new int[] { 0 }, new int[] { 4 }, true);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testCopySubMatrix
    public void testCopySubMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        checkCopy(m, subRows23Cols00,  2 , 3 , 0, 0, false);
        checkCopy(m, subRows00Cols33,  0 , 0 , 3, 3, false);
        checkCopy(m, subRows01Cols23,  0 , 1 , 2, 3, false);
        checkCopy(m, subRows02Cols13,  new int[] { 0, 2 }, new int[] { 1, 3 },    false);
        checkCopy(m, subRows03Cols12,  new int[] { 0, 3 }, new int[] { 1, 2 },    false);
        checkCopy(m, subRows03Cols123, new int[] { 0, 3 }, new int[] { 1, 2, 3 }, false);
        checkCopy(m, subRows20Cols123, new int[] { 2, 0 }, new int[] { 1, 2, 3 }, false);
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);
        checkCopy(m, subRows31Cols31,  new int[] { 3, 1 }, new int[] { 3, 1 },    false);

        checkCopy(m, null,  1, 0, 2, 4, true);
        checkCopy(m, null, -1, 1, 2, 2, true);
        checkCopy(m, null,  1, 0, 2, 2, true);
        checkCopy(m, null,  1, 0, 2, 4, true);
        checkCopy(m, null, new int[] {},    new int[] { 0 }, true);
        checkCopy(m, null, new int[] { 0 }, new int[] { 4 }, true);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mRow0 = new RealMatrixImpl(subRow0);
        RealMatrix mRow3 = new RealMatrixImpl(subRow3);
        assertEquals("Row0", mRow0,
                m.getRowMatrix(0));
        assertEquals("Row3", mRow3,
                m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mRow3 = new RealMatrixImpl(subRow3);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mColumn1 = new RealMatrixImpl(subColumn1);
        RealMatrix mColumn3 = new RealMatrixImpl(subColumn3);
        assertEquals("Column1", mColumn1,
                m.getColumnMatrix(1));
        assertEquals("Column3", mColumn3,
                m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mColumn3 = new RealMatrixImpl(subColumn3);
        assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertEquals("Row0", mRow0, m.getRowVector(0));
        assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnVector(1));
        assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRow(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        double[] mColumn1 = columnToArray(subColumn1);
        double[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m1 = (RealMatrixImpl) m.copy();
        RealMatrixImpl mt = (RealMatrixImpl) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new RealMatrixImpl(bigSingular)));
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testToString
    public void testToString() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        assertEquals("RealMatrixImpl{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
        m = new RealMatrixImpl();
        assertEquals("RealMatrixImpl{}",
                m.toString());
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        assertEquals(expected, m);

        
        try {
            m.setSubMatrix(testData,1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        RealMatrixImpl m2 = new RealMatrixImpl();
        try {
            m2.setSubMatrix(testData,0,1);
            fail("expecting IllegalStateException");
        } catch (IllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            fail("expecting IllegalStateException");
        } catch (IllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.RealMatrixImplTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new RealMatrixImpl(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new RealMatrixImpl(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new RealMatrixImpl(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new RealMatrixImpl(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        assertEquals(rows * columns, getVisitor.getCount());

        m = new RealMatrixImpl(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            assertEquals(0.0, m.getEntry(i, 0), 0);
            assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            assertEquals(0.0, m.getEntry(0, j), 0);
            assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSerial
    public void testSerial()  {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testMoreRows
    public void testMoreRows() {
        final double[] singularValues = { 123.456, 2.3, 1.001, 0.999 };
        final int rows    = singularValues.length + 2;
        final int columns = singularValues.length;
        Random r = new Random(15338437322523l);
        SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(createTestMatrix(r, rows, columns, singularValues));
        double[] computedSV = svd.getSingularValues();
        assertEquals(singularValues.length, computedSV.length);
        for (int i = 0; i < singularValues.length; ++i) {
            assertEquals(singularValues[i], computedSV[i], 1.0e-10);
        }
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testMoreColumns
    public void testMoreColumns() {
        final double[] singularValues = { 123.456, 2.3, 1.001, 0.999 };
        final int rows    = singularValues.length;
        final int columns = singularValues.length + 2;
        Random r = new Random(732763225836210l);
        SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(createTestMatrix(r, rows, columns, singularValues));
        double[] computedSV = svd.getSingularValues();
        assertEquals(singularValues.length, computedSV.length);
        for (int i = 0; i < singularValues.length; ++i) {
            assertEquals(singularValues[i], computedSV[i], 1.0e-10);
        }
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testDimensions
    public void testDimensions() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(testSquare);
        final int m = matrix.getRowDimension();
        final int n = matrix.getColumnDimension();
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        assertEquals(m, svd.getU().getRowDimension());
        assertEquals(m, svd.getU().getColumnDimension());
        assertEquals(m, svd.getS().getColumnDimension());
        assertEquals(n, svd.getS().getColumnDimension());
        assertEquals(n, svd.getV().getRowDimension());
        assertEquals(n, svd.getV().getColumnDimension());

    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testHadamard
    public void testHadamard() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
                {15.0 / 2.0,  5.0 / 2.0,  9.0 / 2.0,  3.0 / 2.0 },
                { 5.0 / 2.0, 15.0 / 2.0,  3.0 / 2.0,  9.0 / 2.0 },
                { 9.0 / 2.0,  3.0 / 2.0, 15.0 / 2.0,  5.0 / 2.0 },
                { 3.0 / 2.0,  9.0 / 2.0,  5.0 / 2.0, 15.0 / 2.0 }
        }, false);
        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        assertEquals(16.0, svd.getSingularValues()[0], 1.0e-14);
        assertEquals( 8.0, svd.getSingularValues()[1], 1.0e-14);
        assertEquals( 4.0, svd.getSingularValues()[2], 1.0e-14);
        assertEquals( 2.0, svd.getSingularValues()[3], 1.0e-14);

        RealMatrix fullCovariance = new Array2DRowRealMatrix(new double[][] {
                {  85.0 / 1024, -51.0 / 1024, -75.0 / 1024,  45.0 / 1024 },
                { -51.0 / 1024,  85.0 / 1024,  45.0 / 1024, -75.0 / 1024 },
                { -75.0 / 1024,  45.0 / 1024,  85.0 / 1024, -51.0 / 1024 },
                {  45.0 / 1024, -75.0 / 1024, -51.0 / 1024,  85.0 / 1024 }
        }, false);
        assertEquals(0.0,
                     fullCovariance.subtract(svd.getCovariance(0.0)).getNorm(),
                     1.0e-14);

        RealMatrix halfCovariance = new Array2DRowRealMatrix(new double[][] {
                {   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024 },
                {  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024 },
                {   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024 },
                {  -3.0 / 1024,   5.0 / 1024,  -3.0 / 1024,   5.0 / 1024 }
        }, false);
        assertEquals(0.0,
                     halfCovariance.subtract(svd.getCovariance(6.0)).getNorm(),
                     1.0e-14);

    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testAEqualUSVt
    public void testAEqualUSVt() {
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testSquare));
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testNonSquare));
        checkAEqualUSVt(MatrixUtils.createRealMatrix(testNonSquare).transpose());
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testUOrthogonal
    public void testUOrthogonal() {
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare)).getU());
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testNonSquare)).getU());
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testNonSquare).transpose()).getU());
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testVOrthogonal
    public void testVOrthogonal() {
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare)).getV());
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testNonSquare)).getV());
        checkOrthogonal(new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testNonSquare).transpose()).getV());
    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testMatricesValues1
    public void testMatricesValues1() {
       SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare));
        RealMatrix uRef = MatrixUtils.createRealMatrix(new double[][] {
                { 3.0 / 5.0, -4.0 / 5.0 },
                { 4.0 / 5.0,  3.0 / 5.0 }
        });
        RealMatrix sRef = MatrixUtils.createRealMatrix(new double[][] {
                { 3.0, 0.0 },
                { 0.0, 1.0 }
        });
        RealMatrix vRef = MatrixUtils.createRealMatrix(new double[][] {
                { 4.0 / 5.0,  3.0 / 5.0 },
                { 3.0 / 5.0, -4.0 / 5.0 }
        });

        
        RealMatrix u = svd.getU();
        assertEquals(0, u.subtract(uRef).getNorm(), normTolerance);
        RealMatrix s = svd.getS();
        assertEquals(0, s.subtract(sRef).getNorm(), normTolerance);
        RealMatrix v = svd.getV();
        assertEquals(0, v.subtract(vRef).getNorm(), normTolerance);

        
        assertTrue(u == svd.getU());
        assertTrue(s == svd.getS());
        assertTrue(v == svd.getV());

    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testMatricesValues2
    public void testMatricesValues2() {

        RealMatrix uRef = MatrixUtils.createRealMatrix(new double[][] {
            {  0.0 / 5.0,  3.0 / 5.0,  0.0 / 5.0 },
            { -4.0 / 5.0,  0.0 / 5.0, -3.0 / 5.0 },
            {  0.0 / 5.0,  4.0 / 5.0,  0.0 / 5.0 },
            { -3.0 / 5.0,  0.0 / 5.0,  4.0 / 5.0 }
        });
        RealMatrix sRef = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 0.0, 0.0 },
            { 0.0, 3.0, 0.0 },
            { 0.0, 0.0, 2.0 }
        });
        RealMatrix vRef = MatrixUtils.createRealMatrix(new double[][] {
            {  80.0 / 125.0,  -60.0 / 125.0, 75.0 / 125.0 },
            {  24.0 / 125.0,  107.0 / 125.0, 60.0 / 125.0 },
            { -93.0 / 125.0,  -24.0 / 125.0, 80.0 / 125.0 }
        });

        
        SingularValueDecomposition svd =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testNonSquare));
        RealMatrix u = svd.getU();
        assertEquals(0, u.subtract(uRef).getNorm(), normTolerance);
        RealMatrix s = svd.getS();
        assertEquals(0, s.subtract(sRef).getNorm(), normTolerance);
        RealMatrix v = svd.getV();
        assertEquals(0, v.subtract(vRef).getNorm(), normTolerance);

        
        assertTrue(u == svd.getU());
        assertTrue(s == svd.getS());
        assertTrue(v == svd.getV());

    }

// org.apache.commons.math.linear.SingularValueDecompositionImplTest::testConditionNumber
    public void testConditionNumber() {
        SingularValueDecompositionImpl svd =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare));
        assertEquals(3.0, svd.getConditionNumber(), 1.0e-15);
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testSolveDimensionErrors
    public void testSolveDimensionErrors() {
        DecompositionSolver solver =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[3][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (IllegalArgumentException iae) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testSolveSingularityErrors
    public void testSolveSingularityErrors() {
        RealMatrix m =
            MatrixUtils.createRealMatrix(new double[][] {
                                   { 1.0, 0.0 },
                                   { 0.0, 0.0 }
                               });
        DecompositionSolver solver = new SingularValueDecompositionImpl(m).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[2][2]);
        try {
            solver.solve(b);
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumn(0));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(b.getColumnVector(0));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
        try {
            solver.solve(new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(0)));
            fail("an exception should have been thrown");
        } catch (InvalidMatrixException ime) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.linear.SingularValueSolverTest::testSolve
    public void testSolve() {
        DecompositionSolver solver =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare)).getSolver();
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
                { 1, 2, 3 }, { 0, -5, 1 }
        });
        RealMatrix xRef = MatrixUtils.createRealMatrix(new double[][] {
                { -8.0 / 25.0, -263.0 / 75.0, -29.0 / 75.0 },
                { 19.0 / 25.0,   78.0 / 25.0,  49.0 / 25.0 }
        });

        
        assertEquals(0, solver.solve(b).subtract(xRef).getNorm(), normTolerance);

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         new ArrayRealVector(solver.solve(b.getColumn(i))).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            assertEquals(0,
                         solver.solve(b.getColumnVector(i)).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

        
        for (int i = 0; i < b.getColumnDimension(); ++i) {
            ArrayRealVectorTest.RealVectorTestImpl v =
                new ArrayRealVectorTest.RealVectorTestImpl(b.getColumn(i));
            assertEquals(0,
                         solver.solve(v).subtract(xRef.getColumnVector(i)).getNorm(),
                         1.0e-13);
        }

    }

// org.apache.commons.math.linear.SingularValueSolverTest::testConditionNumber
    public void testConditionNumber() {
        SingularValueDecompositionImpl svd =
            new SingularValueDecompositionImpl(MatrixUtils.createRealMatrix(testSquare));
        assertEquals(3.0, svd.getConditionNumber(), 1.0e-15);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testDimensions
    public void testDimensions() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        assertEquals("testData row dimension", 3, m.getRowDimension());
        assertEquals("testData column dimension", 3, m.getColumnDimension());
        assertTrue("testData is square", m.isSquare());
        assertEquals("testData2 row dimension", m2.getRowDimension(), 2);
        assertEquals("testData2 column dimension", m2.getColumnDimension(), 3);
        assertTrue("testData2 is not square", !m2.isSquare());
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        OpenMapRealMatrix m1 = createSparseMatrix(testData);
        RealMatrix m2 = m1.copy();
        assertEquals(m1.getClass(), m2.getClass());
        assertEquals((m2), m1);
        OpenMapRealMatrix m3 = createSparseMatrix(testData);
        RealMatrix m4 = m3.copy();
        assertEquals(m3.getClass(), m4.getClass());
        assertEquals((m4), m3);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testAdd
    public void testAdd() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix mInv = createSparseMatrix(testDataInv);
        OpenMapRealMatrix mDataPlusInv = createSparseMatrix(testDataPlusInv);
        RealMatrix mPlusMInv = m.add(mInv);
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
                    mDataPlusInv.getEntry(row, col), mPlusMInv.getEntry(row, col),
                    entryTolerance);
            }
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testAddFail
    public void testAddFail() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testNorm
    public void testNorm() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        assertEquals("testData norm", 14d, m.getNorm(), entryTolerance);
        assertEquals("testData2 norm", 7d, m2.getNorm(), entryTolerance);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix n = createSparseMatrix(testDataInv);
        assertClose("m-n = m + -n", m.subtract(n),
            n.scalarMultiply(-1d).add(m), entryTolerance);
        try {
            m.subtract(createSparseMatrix(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testMultiply
    public void testMultiply() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix mInv = createSparseMatrix(testDataInv);
        OpenMapRealMatrix identity = createSparseMatrix(id);
        OpenMapRealMatrix m2 = createSparseMatrix(testData2);
        assertClose("inverse multiply", m.multiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", m.multiply(new BlockRealMatrix(testDataInv)), identity,
                    entryTolerance);
        assertClose("inverse multiply", mInv.multiply(m), identity,
                entryTolerance);
        assertClose("identity multiply", m.multiply(identity), m,
                entryTolerance);
        assertClose("identity multiply", identity.multiply(mInv), mInv,
                entryTolerance);
        assertClose("identity multiply", m2.multiply(identity), m2,
                entryTolerance);
        try {
            m.multiply(createSparseMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testMultiply2
    public void testMultiply2() {
        RealMatrix m3 = createSparseMatrix(d3);
        RealMatrix m4 = createSparseMatrix(d4);
        RealMatrix m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = createSparseMatrix(id);
        assertEquals("identity trace", 3d, m.getTrace(), entryTolerance);
        m = createSparseMatrix(testData2);
        try {
            m.getTrace();
            fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = createSparseMatrix(testData);
        assertClose("scalar add", createSparseMatrix(testDataPlus2),
            m.scalarAdd(2d), entryTolerance);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testOperate
    public void testOperate() {
        RealMatrix m = createSparseMatrix(id);
        assertClose("identity operate", testVector, m.operate(testVector),
                entryTolerance);
        assertClose("identity operate", testVector, m.operate(
                new ArrayRealVector(testVector)).getData(), entryTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = createSparseMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 } });
        double[] b = a.operate(new double[] { 1, 1 });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals(3.0, b[0], 1.0e-12);
        assertEquals(7.0, b[1], 1.0e-12);
        assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testTranspose
    public void testTranspose() {

        RealMatrix m = createSparseMatrix(testData);
        RealMatrix mIT = new LUDecompositionImpl(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecompositionImpl(m.transpose()).getSolver().getInverse();
        assertClose("inverse-transpose", mIT, mTI, normTolerance);
        m = createSparseMatrix(testData2);
        RealMatrix mt = createSparseMatrix(testData2T);
        assertClose("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = createSparseMatrix(testData);
        assertClose("premultiply", m.preMultiply(testVector), preMultTest,
            normTolerance);
        assertClose("premultiply", m.preMultiply(
            new ArrayRealVector(testVector).getData()), preMultTest, normTolerance);
        m = createSparseMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = createSparseMatrix(d3);
        RealMatrix m4 = createSparseMatrix(d4);
        RealMatrix m5 = createSparseMatrix(d5);
        assertClose("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);

        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix mInv = createSparseMatrix(testDataInv);
        OpenMapRealMatrix identity = createSparseMatrix(id);
        assertClose("inverse multiply", m.preMultiply(mInv), identity,
                entryTolerance);
        assertClose("inverse multiply", mInv.preMultiply(m), identity,
                entryTolerance);
        assertClose("identity multiply", m.preMultiply(identity), m,
                entryTolerance);
        assertClose("identity multiply", identity.preMultiply(mInv), mInv,
                entryTolerance);
        try {
            m.preMultiply(createSparseMatrix(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = createSparseMatrix(testData);
        assertClose("get row", m.getRow(0), testDataRow1, entryTolerance);
        assertClose("get col", m.getColumn(2), testDataCol3, entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = createSparseMatrix(testData);
        assertEquals("get entry", m.getEntry(0, 1), 2d, entryTolerance);
        try {
            m.getEntry(10, 4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { { 1d, 2d, 3d }, { 2d, 5d, 3d } };
        RealMatrix m = createSparseMatrix(matrixData);
        
        double[][] matrixData2 = { { 1d, 2d }, { 2d, 5d }, { 1d, 7d } };
        RealMatrix n = createSparseMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        assertEquals(2, p.getRowDimension());
        assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecompositionImpl(p).getSolver().getInverse();
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = { { 2, 3, -2 }, { -1, 7, 6 },
                { 4, -3, -5 } };
        RealMatrix coefficients = createSparseMatrix(coefficientsData);
        double[] constants = { 1, -2, 1 };
        double[] solution = new LUDecompositionImpl(coefficients).getSolver().solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] - 2 * solution[2],
                constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2],
                constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] - 5 * solution[2],
                constants[2], 1E-12);

    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testSubMatrix
    public void testSubMatrix() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealMatrix mRows23Cols00 = createSparseMatrix(subRows23Cols00);
        RealMatrix mRows00Cols33 = createSparseMatrix(subRows00Cols33);
        RealMatrix mRows01Cols23 = createSparseMatrix(subRows01Cols23);
        RealMatrix mRows02Cols13 = createSparseMatrix(subRows02Cols13);
        RealMatrix mRows03Cols12 = createSparseMatrix(subRows03Cols12);
        RealMatrix mRows03Cols123 = createSparseMatrix(subRows03Cols123);
        RealMatrix mRows20Cols123 = createSparseMatrix(subRows20Cols123);
        RealMatrix mRows31Cols31 = createSparseMatrix(subRows31Cols31);
        assertEquals("Rows23Cols00", mRows23Cols00, m.getSubMatrix(2, 3, 0, 0));
        assertEquals("Rows00Cols33", mRows00Cols33, m.getSubMatrix(0, 0, 3, 3));
        assertEquals("Rows01Cols23", mRows01Cols23, m.getSubMatrix(0, 1, 2, 3));
        assertEquals("Rows02Cols13", mRows02Cols13,
            m.getSubMatrix(new int[] { 0, 2 }, new int[] { 1, 3 }));
        assertEquals("Rows03Cols12", mRows03Cols12,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2 }));
        assertEquals("Rows03Cols123", mRows03Cols123,
            m.getSubMatrix(new int[] { 0, 3 }, new int[] { 1, 2, 3 }));
        assertEquals("Rows20Cols123", mRows20Cols123,
            m.getSubMatrix(new int[] { 2, 0 }, new int[] { 1, 2, 3 }));
        assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));
        assertEquals("Rows31Cols31", mRows31Cols31,
            m.getSubMatrix(new int[] { 3, 1 }, new int[] { 3, 1 }));

        try {
            m.getSubMatrix(1, 0, 2, 4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(-1, 1, 2, 2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1, 0, 2, 4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] { 0 });
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] { 0 }, new int[] { 4 });
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealMatrix mRow0 = createSparseMatrix(subRow0);
        RealMatrix mRow3 = createSparseMatrix(subRow3);
        assertEquals("Row0", mRow0, m.getRowMatrix(0));
        assertEquals("Row3", mRow3, m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealMatrix mColumn1 = createSparseMatrix(subColumn1);
        RealMatrix mColumn3 = createSparseMatrix(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnMatrix(1));
        assertEquals("Column3", mColumn3, m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        assertEquals("Row0", mRow0, m.getRowVector(0));
        assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getRowVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = createSparseMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        assertEquals("Column1", mColumn1, m.getColumnVector(1));
        assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getColumnVector(4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        OpenMapRealMatrix m1 = m.copy();
        OpenMapRealMatrix mt = (OpenMapRealMatrix) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(createSparseMatrix(bigSingular)));
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testToString
    public void testToString() {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        assertEquals("OpenMapRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
            m.toString());
        m = new OpenMapRealMatrix(1, 1);
        assertEquals("OpenMapRealMatrix{{0.0}}", m.toString());
    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        m.setSubMatrix(detData2, 1, 1);
        RealMatrix expected = createSparseMatrix(new double[][] {
                { 1.0, 2.0, 3.0 }, { 2.0, 1.0, 3.0 }, { 1.0, 2.0, 4.0 } });
        assertEquals(expected, m);

        m.setSubMatrix(detData2, 0, 0);
        expected = createSparseMatrix(new double[][] {
                { 1.0, 3.0, 3.0 }, { 2.0, 4.0, 3.0 }, { 1.0, 2.0, 4.0 } });
        assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2, 0, 0);
        expected = createSparseMatrix(new double[][] {
                { 3.0, 4.0, 5.0 }, { 4.0, 7.0, 5.0 }, { 3.0, 2.0, 10.0 } });
        assertEquals(expected, m);

        
        OpenMapRealMatrix matrix =
            createSparseMatrix(new double[][] {
        { 1, 2, 3, 4 }, { 5, 6, 7, 8 }, { 9, 0, 1, 2 } });
        matrix.setSubMatrix(new double[][] { { 3, 4 }, { 5, 6 } }, 1, 1);
        expected = createSparseMatrix(new double[][] {
                { 1, 2, 3, 4 }, { 5, 3, 4, 8 }, { 9, 5, 6, 2 } });
        assertEquals(expected, matrix);

        
        try {
            m.setSubMatrix(testData, 1, 1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        try {
            m.setSubMatrix(testData, -1, 1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m.setSubMatrix(testData, 1, -1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }

        
        try {
            m.setSubMatrix(null, 1, 1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        try {
            new OpenMapRealMatrix(0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] { { 1 }, { 2, 3 } }, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] { {} }, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

    }

// org.apache.commons.math.linear.SparseRealMatrixTest::testSerial
    public void testSerial()  {
        OpenMapRealMatrix m = createSparseMatrix(testData);
        assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math.linear.SparseRealVectorTest::testConstructors
    public void testConstructors() {

        OpenMapRealVector v0 = new OpenMapRealVector();
        assertEquals("testData len", 0, v0.getDimension());

        OpenMapRealVector v1 = new OpenMapRealVector(7);
        assertEquals("testData len", 7, v1.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v1.getEntry(6));

        OpenMapRealVector v3 = new OpenMapRealVector(vec1);
        assertEquals("testData len", 3, v3.getDimension());
        assertEquals("testData is 2.0 ", 2.0, v3.getEntry(1));

        
        
        
        
        
        
        
            
        
        
        

        RealVector v5_i = new OpenMapRealVector(dvec1);
        assertEquals("testData len", 9, v5_i.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v5_i.getEntry(8));

        OpenMapRealVector v5 = new OpenMapRealVector(dvec1);
        assertEquals("testData len", 9, v5.getDimension());
        assertEquals("testData is 9.0 ", 9.0, v5.getEntry(8));

        OpenMapRealVector v7 = new OpenMapRealVector(v1);
        assertEquals("testData len", 7, v7.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v7.getEntry(6));

        SparseRealVectorTestImpl v7_i = new SparseRealVectorTestImpl(vec1);

        OpenMapRealVector v7_2 = new OpenMapRealVector(v7_i);
        assertEquals("testData len", 3, v7_2.getDimension());
        assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1));

        OpenMapRealVector v8 = new OpenMapRealVector(v1);
        assertEquals("testData len", 7, v8.getDimension());
        assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6));

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testDataInOut
    public void testDataInOut() {

        OpenMapRealVector v1 = new OpenMapRealVector(vec1);
        OpenMapRealVector v2 = new OpenMapRealVector(vec2);
        OpenMapRealVector v4 = new OpenMapRealVector(vec4);
        SparseRealVectorTestImpl v2_t = new SparseRealVectorTestImpl(vec2);

        RealVector v_append_1 = v1.append(v2);
        assertEquals("testData len", 6, v_append_1.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v_append_1.getEntry(3));

        RealVector v_append_2 = v1.append(2.0);
        assertEquals("testData len", 4, v_append_2.getDimension());
        assertEquals("testData is 2.0 ", 2.0, v_append_2.getEntry(3));

        RealVector v_append_3 = v1.append(vec2);
        assertEquals("testData len", 6, v_append_3.getDimension());
        assertEquals("testData is  ", 4.0, v_append_3.getEntry(3));

        RealVector v_append_4 = v1.append(v2_t);
        assertEquals("testData len", 6, v_append_4.getDimension());
        assertEquals("testData is 4.0 ", 4.0, v_append_4.getEntry(3));

        RealVector vout5 = v4.getSubVector(3, 3);
        assertEquals("testData len", 3, vout5.getDimension());
        assertEquals("testData is 4.0 ", 5.0, vout5.getEntry(1));
        try {
            v4.getSubVector(3, 7);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        OpenMapRealVector v_set1 = v1.copy();
        v_set1.setEntry(1, 11.0);
        assertEquals("testData is 11.0 ", 11.0, v_set1.getEntry(1));
        try {
            v_set1.setEntry(3, 11.0);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        OpenMapRealVector v_set2 = v4.copy();
        v_set2.setSubVector(3, v1);
        assertEquals("testData is 1.0 ", 1.0, v_set2.getEntry(3));
        assertEquals("testData is 7.0 ", 7.0, v_set2.getEntry(6));
        try {
            v_set2.setSubVector(7, v1);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        OpenMapRealVector v_set3 = v1.copy();
        v_set3.set(13.0);
        assertEquals("testData is 13.0 ", 13.0, v_set3.getEntry(2));

        try {
            v_set3.getEntry(23);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        OpenMapRealVector v_set4 = v4.copy();
        v_set4.setSubVector(3, v2_t);
        assertEquals("testData is 1.0 ", 4.0, v_set4.getEntry(3));
        assertEquals("testData is 7.0 ", 7.0, v_set4.getEntry(6));
        try {
            v_set4.setSubVector(7, v2_t);
            fail("MatrixIndexException expected");
        } catch (MatrixIndexException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testMapFunctions
    public void testMapFunctions() {
        OpenMapRealVector v1 = new OpenMapRealVector(vec1);

        
        RealVector v_mapAdd = v1.mapAdd(2.0d);
        double[] result_mapAdd = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAdd,v_mapAdd.getData(),normTolerance);

        
        RealVector v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(2.0d);
        double[] result_mapAddToSelf = {3d, 4d, 5d};
        assertClose("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData(),normTolerance);

        
        RealVector v_mapSubtract = v1.mapSubtract(2.0d);
        double[] result_mapSubtract = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtract,v_mapSubtract.getData(),normTolerance);

        
        RealVector v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(2.0d);
        double[] result_mapSubtractToSelf = {-1d, 0d, 1d};
        assertClose("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData(),normTolerance);

        
        RealVector v_mapMultiply = v1.mapMultiply(2.0d);
        double[] result_mapMultiply = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiply,v_mapMultiply.getData(),normTolerance);

        
        RealVector v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(2.0d);
        double[] result_mapMultiplyToSelf = {2d, 4d, 6d};
        assertClose("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData(),normTolerance);

        
        RealVector v_mapDivide = v1.mapDivide(2.0d);
        double[] result_mapDivide = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivide,v_mapDivide.getData(),normTolerance);

        
        RealVector v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(2.0d);
        double[] result_mapDivideToSelf = {.5d, 1d, 1.5d};
        assertClose("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData(),normTolerance);

        
        RealVector v_mapPow = v1.mapPow(2.0d);
        double[] result_mapPow = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPow,v_mapPow.getData(),normTolerance);

        
        RealVector v_mapPowToSelf = v1.copy();
        v_mapPowToSelf.mapPowToSelf(2.0d);
        double[] result_mapPowToSelf = {1d, 4d, 9d};
        assertClose("compare vectors" ,result_mapPowToSelf,v_mapPowToSelf.getData(),normTolerance);

        
        RealVector v_mapExp = v1.mapExp();
        double[] result_mapExp = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExp,v_mapExp.getData(),normTolerance);

        
        RealVector v_mapExpToSelf = v1.copy();
        v_mapExpToSelf.mapExpToSelf();
        double[] result_mapExpToSelf = {2.718281828459045e+00d,7.389056098930650e+00d, 2.008553692318767e+01d};
        assertClose("compare vectors" ,result_mapExpToSelf,v_mapExpToSelf.getData(),normTolerance);

        
        RealVector v_mapExpm1 = v1.mapExpm1();
        double[] result_mapExpm1 = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1,v_mapExpm1.getData(),normTolerance);

        
        RealVector v_mapExpm1ToSelf = v1.copy();
        v_mapExpm1ToSelf.mapExpm1ToSelf();
        double[] result_mapExpm1ToSelf = {1.718281828459045d,6.38905609893065d, 19.085536923187668d};
        assertClose("compare vectors" ,result_mapExpm1ToSelf,v_mapExpm1ToSelf.getData(),normTolerance);

        
        RealVector v_mapLog = v1.mapLog();
        double[] result_mapLog = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLog,v_mapLog.getData(),normTolerance);

        
        RealVector v_mapLogToSelf = v1.copy();
        v_mapLogToSelf.mapLogToSelf();
        double[] result_mapLogToSelf = {0d,6.931471805599453e-01d, 1.098612288668110e+00d};
        assertClose("compare vectors" ,result_mapLogToSelf,v_mapLogToSelf.getData(),normTolerance);

        
        RealVector v_mapLog10 = v1.mapLog10();
        double[] result_mapLog10 = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10,v_mapLog10.getData(),normTolerance);

        
        RealVector v_mapLog10ToSelf = v1.copy();
        v_mapLog10ToSelf.mapLog10ToSelf();
        double[] result_mapLog10ToSelf = {0d,3.010299956639812e-01d, 4.771212547196624e-01d};
        assertClose("compare vectors" ,result_mapLog10ToSelf,v_mapLog10ToSelf.getData(),normTolerance);

        
        RealVector v_mapLog1p = v1.mapLog1p();
        double[] result_mapLog1p = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1p,v_mapLog1p.getData(),normTolerance);

        
        RealVector v_mapLog1pToSelf = v1.copy();
        v_mapLog1pToSelf.mapLog1pToSelf();
        double[] result_mapLog1pToSelf = {0.6931471805599453d,1.0986122886681096d,1.3862943611198906d};
        assertClose("compare vectors" ,result_mapLog1pToSelf,v_mapLog1pToSelf.getData(),normTolerance);

        
        RealVector v_mapCosh = v1.mapCosh();
        double[] result_mapCosh = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCosh,v_mapCosh.getData(),normTolerance);

        
        RealVector v_mapCoshToSelf = v1.copy();
        v_mapCoshToSelf.mapCoshToSelf();
        double[] result_mapCoshToSelf = {1.543080634815244e+00d,3.762195691083631e+00d, 1.006766199577777e+01d};
        assertClose("compare vectors" ,result_mapCoshToSelf,v_mapCoshToSelf.getData(),normTolerance);

        
        RealVector v_mapSinh = v1.mapSinh();
        double[] result_mapSinh = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinh,v_mapSinh.getData(),normTolerance);

        
        RealVector v_mapSinhToSelf = v1.copy();
        v_mapSinhToSelf.mapSinhToSelf();
        double[] result_mapSinhToSelf = {1.175201193643801e+00d,3.626860407847019e+00d, 1.001787492740990e+01d};
        assertClose("compare vectors" ,result_mapSinhToSelf,v_mapSinhToSelf.getData(),normTolerance);

        
        RealVector v_mapTanh = v1.mapTanh();
        double[] result_mapTanh = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanh,v_mapTanh.getData(),normTolerance);

        
        RealVector v_mapTanhToSelf = v1.copy();
        v_mapTanhToSelf.mapTanhToSelf();
        double[] result_mapTanhToSelf = {7.615941559557649e-01d,9.640275800758169e-01d,9.950547536867305e-01d};
        assertClose("compare vectors" ,result_mapTanhToSelf,v_mapTanhToSelf.getData(),normTolerance);

        
        RealVector v_mapCos = v1.mapCos();
        double[] result_mapCos = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCos,v_mapCos.getData(),normTolerance);

        
        RealVector v_mapCosToSelf = v1.copy();
        v_mapCosToSelf.mapCosToSelf();
        double[] result_mapCosToSelf = {5.403023058681398e-01d,-4.161468365471424e-01d, -9.899924966004454e-01d};
        assertClose("compare vectors" ,result_mapCosToSelf,v_mapCosToSelf.getData(),normTolerance);

        
        RealVector v_mapSin = v1.mapSin();
        double[] result_mapSin = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSin,v_mapSin.getData(),normTolerance);

        
        RealVector v_mapSinToSelf = v1.copy();
        v_mapSinToSelf.mapSinToSelf();
        double[] result_mapSinToSelf = {8.414709848078965e-01d,9.092974268256817e-01d,1.411200080598672e-01d};
        assertClose("compare vectors" ,result_mapSinToSelf,v_mapSinToSelf.getData(),normTolerance);

        
        RealVector v_mapTan = v1.mapTan();
        double[] result_mapTan = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTan,v_mapTan.getData(),normTolerance);

        
        RealVector v_mapTanToSelf = v1.copy();
        v_mapTanToSelf.mapTanToSelf();
        double[] result_mapTanToSelf = {1.557407724654902e+00d,-2.185039863261519e+00d,-1.425465430742778e-01d};
        assertClose("compare vectors" ,result_mapTanToSelf,v_mapTanToSelf.getData(),normTolerance);

        double[] vat_a = {0d, 0.5d, 1.0d};
        OpenMapRealVector vat = new OpenMapRealVector(vat_a);

        
        RealVector v_mapAcos = vat.mapAcos();
        double[] result_mapAcos = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcos,v_mapAcos.getData(),normTolerance);

        
        RealVector v_mapAcosToSelf = vat.copy();
        v_mapAcosToSelf.mapAcosToSelf();
        double[] result_mapAcosToSelf = {1.570796326794897e+00d,1.047197551196598e+00d, 0.0d};
        assertClose("compare vectors" ,result_mapAcosToSelf,v_mapAcosToSelf.getData(),normTolerance);

        
        RealVector v_mapAsin = vat.mapAsin();
        double[] result_mapAsin = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsin,v_mapAsin.getData(),normTolerance);

        
        RealVector v_mapAsinToSelf = vat.copy();
        v_mapAsinToSelf.mapAsinToSelf();
        double[] result_mapAsinToSelf = {0.0d,5.235987755982989e-01d,1.570796326794897e+00d};
        assertClose("compare vectors" ,result_mapAsinToSelf,v_mapAsinToSelf.getData(),normTolerance);

        
        RealVector v_mapAtan = vat.mapAtan();
        double[] result_mapAtan = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtan,v_mapAtan.getData(),normTolerance);

        
        RealVector v_mapAtanToSelf = vat.copy();
        v_mapAtanToSelf.mapAtanToSelf();
        double[] result_mapAtanToSelf = {0.0d,4.636476090008061e-01d,7.853981633974483e-01d};
        assertClose("compare vectors" ,result_mapAtanToSelf,v_mapAtanToSelf.getData(),normTolerance);

        
        RealVector v_mapInv = v1.mapInv();
        double[] result_mapInv = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInv,v_mapInv.getData(),normTolerance);

        
        RealVector v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapInvToSelf();
        double[] result_mapInvToSelf = {1d,0.5d,3.333333333333333e-01d};
        assertClose("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData(),normTolerance);

        double[] abs_a = {-1.0d, 0.0d, 1.0d};
        OpenMapRealVector abs_v = new OpenMapRealVector(abs_a);

        
        RealVector v_mapAbs = abs_v.mapAbs();
        double[] result_mapAbs = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbs,v_mapAbs.getData(),normTolerance);

        
        RealVector v_mapAbsToSelf = abs_v.copy();
        v_mapAbsToSelf.mapAbsToSelf();
        double[] result_mapAbsToSelf = {1d,0d,1d};
        assertClose("compare vectors" ,result_mapAbsToSelf,v_mapAbsToSelf.getData(),normTolerance);

        
        RealVector v_mapSqrt = v1.mapSqrt();
        double[] result_mapSqrt = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrt,v_mapSqrt.getData(),normTolerance);

        
        RealVector v_mapSqrtToSelf = v1.copy();
        v_mapSqrtToSelf.mapSqrtToSelf();
        double[] result_mapSqrtToSelf = {1d,1.414213562373095e+00d,1.732050807568877e+00d};
        assertClose("compare vectors" ,result_mapSqrtToSelf,v_mapSqrtToSelf.getData(),normTolerance);

        double[] cbrt_a = {-2.0d, 0.0d, 2.0d};
        OpenMapRealVector cbrt_v = new OpenMapRealVector(cbrt_a);

        
        RealVector v_mapCbrt = cbrt_v.mapCbrt();
        double[] result_mapCbrt = {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrt,v_mapCbrt.getData(),normTolerance);

        
        RealVector v_mapCbrtToSelf = cbrt_v.copy();
        v_mapCbrtToSelf.mapCbrtToSelf();
        double[] result_mapCbrtToSelf =  {-1.2599210498948732d,0d,1.2599210498948732d};
        assertClose("compare vectors" ,result_mapCbrtToSelf,v_mapCbrtToSelf.getData(),normTolerance);

        double[] ceil_a = {-1.1d, 0.9d, 1.1d};
        OpenMapRealVector ceil_v = new OpenMapRealVector(ceil_a);

        
        RealVector v_mapCeil = ceil_v.mapCeil();
        double[] result_mapCeil = {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeil,v_mapCeil.getData(),normTolerance);

        
        RealVector v_mapCeilToSelf = ceil_v.copy();
        v_mapCeilToSelf.mapCeilToSelf();
        double[] result_mapCeilToSelf =  {-1d,1d,2d};
        assertClose("compare vectors" ,result_mapCeilToSelf,v_mapCeilToSelf.getData(),normTolerance);

        
        RealVector v_mapFloor = ceil_v.mapFloor();
        double[] result_mapFloor = {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloor,v_mapFloor.getData(),normTolerance);

        
        RealVector v_mapFloorToSelf = ceil_v.copy();
        v_mapFloorToSelf.mapFloorToSelf();
        double[] result_mapFloorToSelf =  {-2d,0d,1d};
        assertClose("compare vectors" ,result_mapFloorToSelf,v_mapFloorToSelf.getData(),normTolerance);

        
        RealVector v_mapRint = ceil_v.mapRint();
        double[] result_mapRint = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRint,v_mapRint.getData(),normTolerance);

        
        RealVector v_mapRintToSelf = ceil_v.copy();
        v_mapRintToSelf.mapRintToSelf();
        double[] result_mapRintToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapRintToSelf,v_mapRintToSelf.getData(),normTolerance);

        
        RealVector v_mapSignum = ceil_v.mapSignum();
        double[] result_mapSignum = {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignum,v_mapSignum.getData(),normTolerance);

        
        RealVector v_mapSignumToSelf = ceil_v.copy();
        v_mapSignumToSelf.mapSignumToSelf();
        double[] result_mapSignumToSelf =  {-1d,1d,1d};
        assertClose("compare vectors" ,result_mapSignumToSelf,v_mapSignumToSelf.getData(),normTolerance);

        
        
        RealVector v_mapUlp = ceil_v.mapUlp();
        double[] result_mapUlp = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlp,v_mapUlp.getData(),normTolerance);

        
        RealVector v_mapUlpToSelf = ceil_v.copy();
        v_mapUlpToSelf.mapUlpToSelf();
        double[] result_mapUlpToSelf = {2.220446049250313E-16d,1.1102230246251565E-16d,2.220446049250313E-16d};
        assertClose("compare vectors" ,result_mapUlpToSelf,v_mapUlpToSelf.getData(),normTolerance);

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testBasicFunctions
    public void testBasicFunctions() {
        OpenMapRealVector v1 = new OpenMapRealVector(vec1);
        OpenMapRealVector v2 = new OpenMapRealVector(vec2);
        OpenMapRealVector v5 = new OpenMapRealVector(vec5);
        OpenMapRealVector v_null = new OpenMapRealVector(vec_null);

        SparseRealVectorTestImpl v2_t = new SparseRealVectorTestImpl(vec2);

        
        double d_getNorm = v5.getNorm();
        assertEquals("compare values  ", 8.4261497731763586307, d_getNorm);

        
        double d_getL1Norm = v5.getL1Norm();
        assertEquals("compare values  ", 17.0, d_getL1Norm);

        
        double d_getLInfNorm = v5.getLInfNorm();
        assertEquals("compare values  ", 6.0, d_getLInfNorm);

        
        double dist = v1.getDistance(v2);
        assertEquals("compare values  ",v1.subtract(v2).getNorm(), dist );

        
        double dist_2 = v1.getDistance(v2_t);
        assertEquals("compare values  ", v1.subtract(v2).getNorm(),dist_2 );

        
        double d_getL1Distance = v1. getL1Distance(v2);
        assertEquals("compare values  ",9d, d_getL1Distance );

        double d_getL1Distance_2 = v1. getL1Distance(v2_t);
        assertEquals("compare values  ",9d, d_getL1Distance_2 );

        
        double d_getLInfDistance = v1. getLInfDistance(v2);
        assertEquals("compare values  ",3d, d_getLInfDistance );

        double d_getLInfDistance_2 = v1. getLInfDistance(v2_t);
        assertEquals("compare values  ",3d, d_getLInfDistance_2 );

        
        OpenMapRealVector v_add = v1.add(v2);
        double[] result_add = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add.getData(),result_add,normTolerance);

        SparseRealVectorTestImpl vt2 = new SparseRealVectorTestImpl(vec2);
        RealVector v_add_i = v1.add(vt2);
        double[] result_add_i = {5d, 7d, 9d};
        assertClose("compare vect" ,v_add_i.getData(),result_add_i,normTolerance);

        
        OpenMapRealVector v_subtract = v1.subtract(v2);
        double[] result_subtract = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract.getData(),result_subtract,normTolerance);

        RealVector v_subtract_i = v1.subtract(vt2);
        double[] result_subtract_i = {-3d, -3d, -3d};
        assertClose("compare vect" ,v_subtract_i.getData(),result_subtract_i,normTolerance);

        
        RealVector  v_ebeMultiply = v1.ebeMultiply(v2);
        double[] result_ebeMultiply = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply,normTolerance);

        RealVector  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        double[] result_ebeMultiply_2 = {4d, 10d, 18d};
        assertClose("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2,normTolerance);

        
        RealVector  v_ebeDivide = v1.ebeDivide(v2);
        double[] result_ebeDivide = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide.getData(),result_ebeDivide,normTolerance);

        RealVector  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        double[] result_ebeDivide_2 = {0.25d, 0.4d, 0.5d};
        assertClose("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2,normTolerance);

        
        double dot =  v1.dotProduct(v2);
        assertEquals("compare val ",32d, dot);

        
        double dot_2 =  v1.dotProduct(v2_t);
        assertEquals("compare val ",32d, dot_2);

        RealMatrix m_outerProduct = v1.outerProduct(v2);
        assertEquals("compare val ",4d, m_outerProduct.getEntry(0,0));

        RealMatrix m_outerProduct_2 = v1.outerProduct(v2_t);
        assertEquals("compare val ",4d, m_outerProduct_2.getEntry(0,0));

        RealVector v_unitVector = v1.unitVector();
        RealVector v_unitVector_2 = v1.mapDivide(v1.getNorm());
        assertClose("compare vect" ,v_unitVector.getData(),v_unitVector_2.getData(),normTolerance);

        try {
            v_null.unitVector();
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        OpenMapRealVector v_unitize = v1.copy();
        v_unitize.unitize();
        assertClose("compare vect" ,v_unitVector_2.getData(),v_unitize.getData(),normTolerance);
        try {
            v_null.unitize();
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

        RealVector v_projection = v1.projection(v2);
        double[] result_projection = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection.getData(), result_projection, normTolerance);

        RealVector v_projection_2 = v1.projection(v2_t);
        double[] result_projection_2 = {1.662337662337662, 2.0779220779220777, 2.493506493506493};
        assertClose("compare vect", v_projection_2.getData(), result_projection_2, normTolerance);

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testMisc
    public void testMisc() {
        OpenMapRealVector v1 = new OpenMapRealVector(vec1);

        String out1 = v1.toString();
        assertTrue("some output ",  out1.length()!=0);
        try {
            v1.checkVectorDimensions(2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testPredicates
    public void testPredicates() {

        OpenMapRealVector v = new OpenMapRealVector(new double[] { 0, 1, 2 });

        assertFalse(v.isNaN());
        v.setEntry(1, Double.NaN);
        assertTrue(v.isNaN());

        assertFalse(v.isInfinite());
        v.setEntry(0, Double.POSITIVE_INFINITY);
        assertFalse(v.isInfinite()); 
        v.setEntry(1, 1);
        assertTrue(v.isInfinite());

        v.setEntry(0, 0);
        assertEquals(v, new OpenMapRealVector(new double[] { 0, 1, 2 }));
        assertNotSame(v, new OpenMapRealVector(new double[] { 0, 1, 2 + Math.ulp(2)}));
        assertNotSame(v, new OpenMapRealVector(new double[] { 0, 1, 2, 3 }));

    }

// org.apache.commons.math.linear.SparseRealVectorTest::testSerial
    public void testSerial()  {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 0, 1, 2 });
        assertEquals(v,TestUtils.serializeAndRecover(v));
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath272
    public void testMath272() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1, 0 }, Relationship.GEQ,  1));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1 }, Relationship.GEQ,  1));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 }, Relationship.GEQ,  1));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertEquals(0.0, solution.getPoint()[0], .0000001);
        Assert.assertEquals(1.0, solution.getPoint()[1], .0000001);
        Assert.assertEquals(1.0, solution.getPoint()[2], .0000001);
        Assert.assertEquals(3.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath286
    public void testMath286() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.6, 0.4 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 23.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0, 0, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 1, 0, 0, 0 }, Relationship.GEQ, 8.0));
        constraints.add(new LinearConstraint(new double[] { 0, 0, 0, 0, 1, 0 }, Relationship.GEQ, 5.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        Assert.assertEquals(25.8, solution.getValue(), .0000001);
        Assert.assertEquals(23.0, solution.getPoint()[0] + solution.getPoint()[2] + solution.getPoint()[4], 0.0000001);
        Assert.assertEquals(23.0, solution.getPoint()[1] + solution.getPoint()[3] + solution.getPoint()[5], 0.0000001);
        Assert.assertTrue(solution.getPoint()[0] >= 10.0 - 0.0000001);
        Assert.assertTrue(solution.getPoint()[2] >= 8.0 - 0.0000001);
        Assert.assertTrue(solution.getPoint()[4] >= 5.0 - 0.0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testDegeneracy
    public void testDegeneracy() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.7 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 18.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 8.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(13.6, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath288
    public void testMath288() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 7, 3, 0, 0 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 3, 0, -5, 0 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 2, 0, 0, -5 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 0, 3, 0, -5 }, Relationship.LEQ, 0.0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0 }, Relationship.LEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 0 }, Relationship.LEQ, 1.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(10.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath290GEQ
    public void testMath290GEQ() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.GEQ, -1.0));
        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(0, solution.getValue(), .0000001);
        Assert.assertEquals(0, solution.getPoint()[0], .0000001);
        Assert.assertEquals(0, solution.getPoint()[1], .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath290LEQ
    public void testMath290LEQ() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 5 }, 0 );
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 0 }, Relationship.LEQ, -1.0));
        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MINIMIZE, true);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMath293
    public void testMath293() throws OptimizationException {
      LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.4, 0.6}, 0 );
      Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0.8, 0.2, 0.0, 0.0, 0.0, 0.0 }, Relationship.GEQ, 10.0));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.7, 0.3, 0.0, 0.0 }, Relationship.GEQ, 10.0));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.4, 0.6 }, Relationship.GEQ, 10.0));

      SimplexSolver solver = new SimplexSolver();
      RealPointValuePair solution1 = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

      Assert.assertEquals(15.7143, solution1.getPoint()[0], .0001);
      Assert.assertEquals(0.0, solution1.getPoint()[1], .0001);
      Assert.assertEquals(14.2857, solution1.getPoint()[2], .0001);
      Assert.assertEquals(0.0, solution1.getPoint()[3], .0001);
      Assert.assertEquals(0.0, solution1.getPoint()[4], .0001);
      Assert.assertEquals(30.0, solution1.getPoint()[5], .0001);
      Assert.assertEquals(40.57143, solution1.getValue(), .0001);

      double valA = 0.8 * solution1.getPoint()[0] + 0.2 * solution1.getPoint()[1];
      double valB = 0.7 * solution1.getPoint()[2] + 0.3 * solution1.getPoint()[3];
      double valC = 0.4 * solution1.getPoint()[4] + 0.6 * solution1.getPoint()[5];

      f = new LinearObjectiveFunction(new double[] { 0.8, 0.2, 0.7, 0.3, 0.4, 0.6}, 0 );
      constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] { 1, 0, 1, 0, 1, 0 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0, 1, 0, 1, 0, 1 }, Relationship.EQ, 30.0));
      constraints.add(new LinearConstraint(new double[] { 0.8, 0.2, 0.0, 0.0, 0.0, 0.0 }, Relationship.GEQ, valA));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.7, 0.3, 0.0, 0.0 }, Relationship.GEQ, valB));
      constraints.add(new LinearConstraint(new double[] { 0.0, 0.0, 0.0, 0.0, 0.4, 0.6 }, Relationship.GEQ, valC));

      RealPointValuePair solution2 = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
      Assert.assertEquals(40.57143, solution2.getValue(), .0001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testSimplexSolver
    public void testSimplexSolver() throws OptimizationException {
        LinearObjectiveFunction f =
            new LinearObjectiveFunction(new double[] { 15, 10 }, 7);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 4));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(2.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(57.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testSingleVariableAndConstraint
    public void testSingleVariableAndConstraint() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 10));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(10.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(30.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testModelWithNoArtificialVars
    public void testModelWithNoArtificialVars() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(2.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(50.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testMinimization
    public void testMinimization() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, -5);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);
        Assert.assertEquals(4.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(0.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(-13.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testSolutionWithNegativeDecisionVariable
    public void testSolutionWithNegativeDecisionVariable() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 6));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 14));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
        Assert.assertEquals(-2.0, solution.getPoint()[0], 0.0);
        Assert.assertEquals(8.0, solution.getPoint()[1], 0.0);
        Assert.assertEquals(12.0, solution.getValue(), 0.0);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testInfeasibleSolution
    public void testInfeasibleSolution() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1 }, Relationship.GEQ, 3));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testUnboundedSolution
    public void testUnboundedSolution() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 15, 10 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 2));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testRestrictVariablesToNonNegative
    public void testRestrictVariablesToNonNegative() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 409, 523, 70, 204, 339 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {    43,   56, 345,  56,    5 }, Relationship.LEQ,  4567456));
        constraints.add(new LinearConstraint(new double[] {    12,   45,   7,  56,   23 }, Relationship.LEQ,    56454));
        constraints.add(new LinearConstraint(new double[] {     8,  768,   0,  34, 7456 }, Relationship.LEQ,  1923421));
        constraints.add(new LinearConstraint(new double[] { 12342, 2342,  34, 678, 2342 }, Relationship.GEQ,     4356));
        constraints.add(new LinearConstraint(new double[] {    45,  678,  76,  52,   23 }, Relationship.EQ,    456356));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(2902.92783505155, solution.getPoint()[0], .0000001);
        Assert.assertEquals(480.419243986254, solution.getPoint()[1], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[2], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[3], .0000001);
        Assert.assertEquals(0.0, solution.getPoint()[4], .0000001);
        Assert.assertEquals(1438556.7491409, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testEpsilon
    public void testEpsilon() throws OptimizationException {
      LinearObjectiveFunction f =
          new LinearObjectiveFunction(new double[] { 10, 5, 1 }, 0);
      Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
      constraints.add(new LinearConstraint(new double[] {  9, 8, 0 }, Relationship.EQ,  17));
      constraints.add(new LinearConstraint(new double[] {  0, 7, 8 }, Relationship.LEQ,  7));
      constraints.add(new LinearConstraint(new double[] { 10, 0, 2 }, Relationship.LEQ, 10));

      SimplexSolver solver = new SimplexSolver();
      RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, false);
      Assert.assertEquals(1.0, solution.getPoint()[0], 0.0);
      Assert.assertEquals(1.0, solution.getPoint()[1], 0.0);
      Assert.assertEquals(0.0, solution.getPoint()[2], 0.0);
      Assert.assertEquals(15.0, solution.getValue(), 0.0);
  }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testTrivialModel
    public void testTrivialModel() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ,  0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(0, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexSolverTest::testLargeModel
    public void testLargeModel() throws OptimizationException {
        double[] objective = new double[] {
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 12, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           12, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 12, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 12, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 12, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 12, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                           1, 1, 1, 1, 1, 1};

        LinearObjectiveFunction f = new LinearObjectiveFunction(objective, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(equationFromString(objective.length, "x0 + x1 + x2 + x3 - x12 = 0"));
        constraints.add(equationFromString(objective.length, "x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 - x13 = 0"));
        constraints.add(equationFromString(objective.length, "x4 + x5 + x6 + x7 + x8 + x9 + x10 + x11 >= 49"));
        constraints.add(equationFromString(objective.length, "x0 + x1 + x2 + x3 >= 42"));
        constraints.add(equationFromString(objective.length, "x14 + x15 + x16 + x17 - x26 = 0"));
        constraints.add(equationFromString(objective.length, "x18 + x19 + x20 + x21 + x22 + x23 + x24 + x25 - x27 = 0"));
        constraints.add(equationFromString(objective.length, "x14 + x15 + x16 + x17 - x12 = 0"));
        constraints.add(equationFromString(objective.length, "x18 + x19 + x20 + x21 + x22 + x23 + x24 + x25 - x13 = 0"));
        constraints.add(equationFromString(objective.length, "x28 + x29 + x30 + x31 - x40 = 0"));
        constraints.add(equationFromString(objective.length, "x32 + x33 + x34 + x35 + x36 + x37 + x38 + x39 - x41 = 0"));
        constraints.add(equationFromString(objective.length, "x32 + x33 + x34 + x35 + x36 + x37 + x38 + x39 >= 49"));
        constraints.add(equationFromString(objective.length, "x28 + x29 + x30 + x31 >= 42"));
        constraints.add(equationFromString(objective.length, "x42 + x43 + x44 + x45 - x54 = 0"));
        constraints.add(equationFromString(objective.length, "x46 + x47 + x48 + x49 + x50 + x51 + x52 + x53 - x55 = 0"));
        constraints.add(equationFromString(objective.length, "x42 + x43 + x44 + x45 - x40 = 0"));
        constraints.add(equationFromString(objective.length, "x46 + x47 + x48 + x49 + x50 + x51 + x52 + x53 - x41 = 0"));
        constraints.add(equationFromString(objective.length, "x56 + x57 + x58 + x59 - x68 = 0"));
        constraints.add(equationFromString(objective.length, "x60 + x61 + x62 + x63 + x64 + x65 + x66 + x67 - x69 = 0"));
        constraints.add(equationFromString(objective.length, "x60 + x61 + x62 + x63 + x64 + x65 + x66 + x67 >= 51"));
        constraints.add(equationFromString(objective.length, "x56 + x57 + x58 + x59 >= 44"));
        constraints.add(equationFromString(objective.length, "x70 + x71 + x72 + x73 - x82 = 0"));
        constraints.add(equationFromString(objective.length, "x74 + x75 + x76 + x77 + x78 + x79 + x80 + x81 - x83 = 0"));
        constraints.add(equationFromString(objective.length, "x70 + x71 + x72 + x73 - x68 = 0"));
        constraints.add(equationFromString(objective.length, "x74 + x75 + x76 + x77 + x78 + x79 + x80 + x81 - x69 = 0"));
        constraints.add(equationFromString(objective.length, "x84 + x85 + x86 + x87 - x96 = 0"));
        constraints.add(equationFromString(objective.length, "x88 + x89 + x90 + x91 + x92 + x93 + x94 + x95 - x97 = 0"));
        constraints.add(equationFromString(objective.length, "x88 + x89 + x90 + x91 + x92 + x93 + x94 + x95 >= 51"));
        constraints.add(equationFromString(objective.length, "x84 + x85 + x86 + x87 >= 44"));
        constraints.add(equationFromString(objective.length, "x98 + x99 + x100 + x101 - x110 = 0"));
        constraints.add(equationFromString(objective.length, "x102 + x103 + x104 + x105 + x106 + x107 + x108 + x109 - x111 = 0"));
        constraints.add(equationFromString(objective.length, "x98 + x99 + x100 + x101 - x96 = 0"));
        constraints.add(equationFromString(objective.length, "x102 + x103 + x104 + x105 + x106 + x107 + x108 + x109 - x97 = 0"));
        constraints.add(equationFromString(objective.length, "x112 + x113 + x114 + x115 - x124 = 0"));
        constraints.add(equationFromString(objective.length, "x116 + x117 + x118 + x119 + x120 + x121 + x122 + x123 - x125 = 0"));
        constraints.add(equationFromString(objective.length, "x116 + x117 + x118 + x119 + x120 + x121 + x122 + x123 >= 49"));
        constraints.add(equationFromString(objective.length, "x112 + x113 + x114 + x115 >= 42"));
        constraints.add(equationFromString(objective.length, "x126 + x127 + x128 + x129 - x138 = 0"));
        constraints.add(equationFromString(objective.length, "x130 + x131 + x132 + x133 + x134 + x135 + x136 + x137 - x139 = 0"));
        constraints.add(equationFromString(objective.length, "x126 + x127 + x128 + x129 - x124 = 0"));
        constraints.add(equationFromString(objective.length, "x130 + x131 + x132 + x133 + x134 + x135 + x136 + x137 - x125 = 0"));
        constraints.add(equationFromString(objective.length, "x140 + x141 + x142 + x143 - x152 = 0"));
        constraints.add(equationFromString(objective.length, "x144 + x145 + x146 + x147 + x148 + x149 + x150 + x151 - x153 = 0"));
        constraints.add(equationFromString(objective.length, "x144 + x145 + x146 + x147 + x148 + x149 + x150 + x151 >= 59"));
        constraints.add(equationFromString(objective.length, "x140 + x141 + x142 + x143 >= 42"));
        constraints.add(equationFromString(objective.length, "x154 + x155 + x156 + x157 - x166 = 0"));
        constraints.add(equationFromString(objective.length, "x158 + x159 + x160 + x161 + x162 + x163 + x164 + x165 - x167 = 0"));
        constraints.add(equationFromString(objective.length, "x154 + x155 + x156 + x157 - x152 = 0"));
        constraints.add(equationFromString(objective.length, "x158 + x159 + x160 + x161 + x162 + x163 + x164 + x165 - x153 = 0"));
        constraints.add(equationFromString(objective.length, "x83 + x82 - x168 = 0"));
        constraints.add(equationFromString(objective.length, "x111 + x110 - x169 = 0"));
        constraints.add(equationFromString(objective.length, "x170 - x182 = 0"));
        constraints.add(equationFromString(objective.length, "x171 - x183 = 0"));
        constraints.add(equationFromString(objective.length, "x172 - x184 = 0"));
        constraints.add(equationFromString(objective.length, "x173 - x185 = 0"));
        constraints.add(equationFromString(objective.length, "x174 - x186 = 0"));
        constraints.add(equationFromString(objective.length, "x175 + x176 - x187 = 0"));
        constraints.add(equationFromString(objective.length, "x177 - x188 = 0"));
        constraints.add(equationFromString(objective.length, "x178 - x189 = 0"));
        constraints.add(equationFromString(objective.length, "x179 - x190 = 0"));
        constraints.add(equationFromString(objective.length, "x180 - x191 = 0"));
        constraints.add(equationFromString(objective.length, "x181 - x192 = 0"));
        constraints.add(equationFromString(objective.length, "x170 - x26 = 0"));
        constraints.add(equationFromString(objective.length, "x171 - x27 = 0"));
        constraints.add(equationFromString(objective.length, "x172 - x54 = 0"));
        constraints.add(equationFromString(objective.length, "x173 - x55 = 0"));
        constraints.add(equationFromString(objective.length, "x174 - x168 = 0"));
        constraints.add(equationFromString(objective.length, "x177 - x169 = 0"));
        constraints.add(equationFromString(objective.length, "x178 - x138 = 0"));
        constraints.add(equationFromString(objective.length, "x179 - x139 = 0"));
        constraints.add(equationFromString(objective.length, "x180 - x166 = 0"));
        constraints.add(equationFromString(objective.length, "x181 - x167 = 0"));
        constraints.add(equationFromString(objective.length, "x193 - x205 = 0"));
        constraints.add(equationFromString(objective.length, "x194 - x206 = 0"));
        constraints.add(equationFromString(objective.length, "x195 - x207 = 0"));
        constraints.add(equationFromString(objective.length, "x196 - x208 = 0"));
        constraints.add(equationFromString(objective.length, "x197 - x209 = 0"));
        constraints.add(equationFromString(objective.length, "x198 + x199 - x210 = 0"));
        constraints.add(equationFromString(objective.length, "x200 - x211 = 0"));
        constraints.add(equationFromString(objective.length, "x201 - x212 = 0"));
        constraints.add(equationFromString(objective.length, "x202 - x213 = 0"));
        constraints.add(equationFromString(objective.length, "x203 - x214 = 0"));
        constraints.add(equationFromString(objective.length, "x204 - x215 = 0"));
        constraints.add(equationFromString(objective.length, "x193 - x182 = 0"));
        constraints.add(equationFromString(objective.length, "x194 - x183 = 0"));
        constraints.add(equationFromString(objective.length, "x195 - x184 = 0"));
        constraints.add(equationFromString(objective.length, "x196 - x185 = 0"));
        constraints.add(equationFromString(objective.length, "x197 - x186 = 0"));
        constraints.add(equationFromString(objective.length, "x198 + x199 - x187 = 0"));
        constraints.add(equationFromString(objective.length, "x200 - x188 = 0"));
        constraints.add(equationFromString(objective.length, "x201 - x189 = 0"));
        constraints.add(equationFromString(objective.length, "x202 - x190 = 0"));
        constraints.add(equationFromString(objective.length, "x203 - x191 = 0"));
        constraints.add(equationFromString(objective.length, "x204 - x192 = 0"));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(7518.0, solution.getValue(), .0000001);
    }

// org.apache.commons.math.optimization.linear.SimplexTableauTest::testInitialization
    public void testInitialization() {
        LinearObjectiveFunction f = createFunction();
        Collection<LinearConstraint> constraints = createConstraints();
        SimplexTableau tableau =
            new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        double[][] expectedInitialTableau = {
                                             {-1, 0,  -1,  -1,  2, 0, 0, 0, -4},
                                             { 0, 1, -15, -10, 25, 0, 0, 0,  0},
                                             { 0, 0,   1,   0, -1, 1, 0, 0,  2},
                                             { 0, 0,   0,   1, -1, 0, 1, 0,  3},
                                             { 0, 0,   1,   1, -2, 0, 0, 1,  4}
        };
        assertMatrixEquals(expectedInitialTableau, tableau.getData());
    }

// org.apache.commons.math.optimization.linear.SimplexTableauTest::testDropPhase1Objective
    public void testDropPhase1Objective() {
        LinearObjectiveFunction f = createFunction();
        Collection<LinearConstraint> constraints = createConstraints();
        SimplexTableau tableau =
            new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        double[][] expectedTableau = {
                                      { 1, -15, -10, 0, 0, 0, 0},
                                      { 0,   1,   0, 1, 0, 0, 2},
                                      { 0,   0,   1, 0, 1, 0, 3},
                                      { 0,   1,   1, 0, 0, 1, 4}
        };
        tableau.dropPhase1Objective();
        assertMatrixEquals(expectedTableau, tableau.getData());
    }

// org.apache.commons.math.optimization.linear.SimplexTableauTest::testTableauWithNoArtificialVars
    public void testTableauWithNoArtificialVars() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {15, 10}, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] {1, 0}, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] {0, 1}, Relationship.LEQ, 3));
        constraints.add(new LinearConstraint(new double[] {1, 1}, Relationship.LEQ, 4));
        SimplexTableau tableau =
            new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        double[][] initialTableau = {
                                     {1, -15, -10, 25, 0, 0, 0, 0},
                                     {0,   1,   0, -1, 1, 0, 0, 2},
                                     {0,   0,   1, -1, 0, 1, 0, 3},
                                     {0,   1,   1, -2, 0, 0, 1, 4}
        };
        assertMatrixEquals(initialTableau, tableau.getData());
    }

// org.apache.commons.math.optimization.linear.SimplexTableauTest::testSerial
    public void testSerial() {
        LinearObjectiveFunction f = createFunction();
        Collection<LinearConstraint> constraints = createConstraints();
        SimplexTableau tableau =
            new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        Assert.assertEquals(tableau, TestUtils.serializeAndRecover(tableau));
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddXSampleData
    public void cannotAddXSampleData() {
        createRegression().newSampleData(new double[]{}, null, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullYSampleData
    public void cannotAddNullYSampleData() {
        createRegression().newSampleData(null, new double[][]{}, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullCovarianceData
    public void cannotAddNullCovarianceData() {
        createRegression().newSampleData(new double[]{}, new double[][]{}, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::notEnoughData
    public void notEnoughData() {
        double[]   reducedY = new double[y.length - 1];
        double[][] reducedX = new double[x.length - 1][];
        double[][] reducedO = new double[omega.length - 1][];
        System.arraycopy(y,     0, reducedY, 0, reducedY.length);
        System.arraycopy(x,     0, reducedX, 0, reducedX.length);
        System.arraycopy(omega, 0, reducedO, 0, reducedO.length);
        createRegression().newSampleData(reducedY, reducedX, reducedO);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataWithSampleSizeMismatch
    public void cannotAddCovarianceDataWithSampleSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[1][];
        omega[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataThatIsNotSquare
    public void cannotAddCovarianceDataThatIsNotSquare() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[3][];
        omega[0] = new double[]{1.0, 0};
        omega[1] = new double[]{0, 1.0};
        omega[2] = new double[]{0, 2.0};
        createRegression().newSampleData(y, x, omega);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::cannotAddXSampleData
    public void cannotAddXSampleData() {
        createRegression().newSampleData(new double[]{}, null);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::cannotAddNullYSampleData
    public void cannotAddNullYSampleData() {
        createRegression().newSampleData(null, new double[][]{});
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::cannotAddSampleDataWithSizeMismatch
    public void cannotAddSampleDataWithSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[1][];
        x[0] = new double[]{1.0, 0};
        createRegression().newSampleData(y, x);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testPerfectFit
    public void testPerfectFit() {
        double[] betaHat = regression.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                               new double[]{ 11.0, 1.0 / 2.0, 2.0 / 3.0, 3.0 / 4.0, 4.0 / 5.0, 5.0 / 6.0 },
                               1e-14);
        double[] residuals = regression.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{0d,0d,0d,0d,0d,0d},
                               1e-14);
        RealMatrix errors =
            new Array2DRowRealMatrix(regression.estimateRegressionParametersVariance(), false);
        final double[] s = { 1.0, -1.0 /  2.0, -1.0 /  3.0, -1.0 /  4.0, -1.0 /  5.0, -1.0 /  6.0 };
        RealMatrix referenceVariance = new Array2DRowRealMatrix(s.length, s.length);
        referenceVariance.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value)
                throws MatrixVisitorException {
                if (row == 0) {
                    return s[column];
                }
                double x = s[row] * s[column];
                return (row == column) ? 2 * x : x;
            }
        });
       assertEquals(0.0,
                     errors.subtract(referenceVariance).getNorm(),
                     5.0e-16 * referenceVariance.getNorm());
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testLongly
    public void testLongly() {
        
        
        double[] design = new double[] {
            60323,83.0,234289,2356,1590,107608,1947,
            61122,88.5,259426,2325,1456,108632,1948,
            60171,88.2,258054,3682,1616,109773,1949,
            61187,89.5,284599,3351,1650,110929,1950,
            63221,96.2,328975,2099,3099,112075,1951,
            63639,98.1,346999,1932,3594,113270,1952,
            64989,99.0,365385,1870,3547,115094,1953,
            63761,100.0,363112,3578,3350,116219,1954,
            66019,101.2,397469,2904,3048,117388,1955,
            67857,104.6,419180,2822,2857,118734,1956,
            68169,108.4,442769,2936,2798,120445,1957,
            66513,110.8,444546,4681,2637,121950,1958,
            68655,112.6,482704,3813,2552,123366,1959,
            69564,114.2,502601,3931,2514,125368,1960,
            69331,115.7,518173,4806,2572,127852,1961,
            70551,116.9,554894,4007,2827,130081,1962
        };

        
        int nobs = 16;
        int nvars = 6;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
          new double[]{-3482258.63459582, 15.0618722713733,
                -0.358191792925910E-01,-2.02022980381683,
                -1.03322686717359,-0.511041056535807E-01,
                 1829.15146461355}, 2E-8); 

        
        double[] residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                267.340029759711,-94.0139423988359,46.28716775752924,
                -410.114621930906,309.7145907602313,-249.3112153297231,
                -164.0489563956039,-13.18035686637081,14.30477260005235,
                 455.394094551857,-17.26892711483297,-39.0550425226967,
                -155.5499735953195,-85.6713080421283,341.9315139607727,
                -206.7578251937366},
                      1E-8);

        
        double[] errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {890420.383607373,
                       84.9149257747669,
                       0.334910077722432E-01,
                       0.488399681651699,
                       0.214274163161675,
                       0.226073200069370,
                       455.478499142212}, errors, 1E-6);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testSwissFertility
    public void testSwissFertility() {
        double[] design = new double[] {
            80.2,17.0,15,12,9.96,
            83.1,45.1,6,9,84.84,
            92.5,39.7,5,5,93.40,
            85.8,36.5,12,7,33.77,
            76.9,43.5,17,15,5.16,
            76.1,35.3,9,7,90.57,
            83.8,70.2,16,7,92.85,
            92.4,67.8,14,8,97.16,
            82.4,53.3,12,7,97.67,
            82.9,45.2,16,13,91.38,
            87.1,64.5,14,6,98.61,
            64.1,62.0,21,12,8.52,
            66.9,67.5,14,7,2.27,
            68.9,60.7,19,12,4.43,
            61.7,69.3,22,5,2.82,
            68.3,72.6,18,2,24.20,
            71.7,34.0,17,8,3.30,
            55.7,19.4,26,28,12.11,
            54.3,15.2,31,20,2.15,
            65.1,73.0,19,9,2.84,
            65.5,59.8,22,10,5.23,
            65.0,55.1,14,3,4.52,
            56.6,50.9,22,12,15.14,
            57.4,54.1,20,6,4.20,
            72.5,71.2,12,1,2.40,
            74.2,58.1,14,8,5.23,
            72.0,63.5,6,3,2.56,
            60.5,60.8,16,10,7.72,
            58.3,26.8,25,19,18.46,
            65.4,49.5,15,8,6.10,
            75.5,85.9,3,2,99.71,
            69.3,84.9,7,6,99.68,
            77.3,89.7,5,2,100.00,
            70.5,78.2,12,6,98.96,
            79.4,64.9,7,3,98.22,
            65.0,75.9,9,9,99.06,
            92.2,84.6,3,3,99.46,
            79.3,63.1,13,13,96.83,
            70.4,38.4,26,12,5.62,
            65.7,7.7,29,11,13.79,
            72.7,16.7,22,13,11.22,
            64.4,17.6,35,32,16.92,
            77.6,37.6,15,7,4.97,
            67.6,18.7,25,7,8.65,
            35.0,1.2,37,53,42.34,
            44.7,46.6,16,29,50.43,
            42.8,27.7,22,29,58.33
        };

        
        int nobs = 47;
        int nvars = 4;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat,
                new double[]{91.05542390271397,
                -0.22064551045715,
                -0.26058239824328,
                -0.96161238456030,
                 0.12441843147162}, 1E-12);

        
        double[] residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                7.1044267859730512,1.6580347433531366,
                4.6944952770029644,8.4548022690166160,13.6547432343186212,
               -9.3586864458500774,7.5822446330520386,15.5568995563859289,
                0.8113090736598980,7.1186762732484308,7.4251378771228724,
                2.6761316873234109,0.8351584810309354,7.1769991119615177,
               -3.8746753206299553,-3.1337779476387251,-0.1412575244091504,
                1.1186809170469780,-6.3588097346816594,3.4039270429434074,
                2.3374058329820175,-7.9272368576900503,-7.8361010968497959,
               -11.2597369269357070,0.9445333697827101,6.6544245101380328,
               -0.9146136301118665,-4.3152449403848570,-4.3536932047009183,
               -3.8907885169304661,-6.3027643926302188,-7.8308982189289091,
               -3.1792280015332750,-6.7167298771158226,-4.8469946718041754,
               -10.6335664353633685,11.1031134362036958,6.0084032641811733,
                5.4326230830188482,-7.2375578629692230,2.1671550814448222,
                15.0147574652763112,4.8625103516321015,-7.1597256413907706,
                -0.4515205619767598,-10.2916870903837587,-15.7812984571900063},
                1E-12);

        
        double[] errors = model.estimateRegressionParametersStandardErrors();
        TestUtils.assertEquals(new double[] {6.94881329475087,
                0.07360008972340,
                0.27410957467466,
                0.19454551679325,
                0.03726654773803}, errors, 1E-10);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testHat
    public void testHat() throws Exception {

        
        double[] design = new double[] {
                11.14, .499, 11.1,
                12.74, .558, 8.9,
                13.13, .604, 8.8,
                11.51, .441, 8.9,
                12.38, .550, 8.8,
                12.60, .528, 9.9,
                11.13, .418, 10.7,
                11.7, .480, 10.5,
                11.02, .406, 10.5,
                11.41, .467, 10.7
        };

        int nobs = 10;
        int nvars = 2;

        
        OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.newSampleData(design, nobs, nvars);

        RealMatrix hat = model.calculateHat();

        
        double[] referenceData = new double[] {
                .418, -.002,  .079, -.274, -.046,  .181,  .128,  .222,  .050,  .242,
                       .242,  .292,  .136,  .243,  .128, -.041,  .033, -.035,  .004,
                              .417, -.019,  .273,  .187, -.126,  .044, -.153,  .004,
                                     .604,  .197, -.038,  .168, -.022,  .275, -.028,
                                            .252,  .111, -.030,  .019, -.010, -.010,
                                                   .148,  .042,  .117,  .012,  .111,
                                                          .262,  .145,  .277,  .174,
                                                                 .154,  .120,  .168,
                                                                        .315,  .148,
                                                                               .187
        };

        
        int k = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = i; j < 10; j++) {
                assertEquals(referenceData[k], hat.getEntry(i, j), 10e-3);
                assertEquals(hat.getEntry(i, j), hat.getEntry(j, i), 10e-12);
                k++;
            }
        }

        
        double[] residuals = model.estimateResiduals();
        RealMatrix I = MatrixUtils.createRealIdentityMatrix(10);
        double[] hatResiduals = I.subtract(hat).operate(model.Y).getData();
        TestUtils.assertEquals(residuals, hatResiduals, 10e-12);
    }
