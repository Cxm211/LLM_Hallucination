// buggy code
    public double[][] getCovariances(EstimationProblem problem)
      throws EstimationException {
 
        // set up the jacobian
        updateJacobian();

        // compute transpose(J).J, avoiding building big intermediate matrices
        final int rows = problem.getMeasurements().length;
        final int cols = problem.getAllParameters().length;
        final int max  = cols * rows;
        double[][] jTj = new double[cols][cols];
        for (int i = 0; i < cols; ++i) {
            for (int j = i; j < cols; ++j) {
                double sum = 0;
                for (int k = 0; k < max; k += cols) {
                    sum += jacobian[k + i] * jacobian[k + j];
                }
                jTj[i][j] = sum;
                jTj[j][i] = sum;
            }
        }

        try {
            // compute the covariances matrix
            return new RealMatrixImpl(jTj).inverse().getData();
        } catch (InvalidMatrixException ime) {
            throw new EstimationException("unable to compute covariances: singular problem",
                                          new Object[0]);
        }

    }

    public double[] guessParametersErrors(EstimationProblem problem)
      throws EstimationException {
        int m = problem.getMeasurements().length;
        int p = problem.getAllParameters().length;
        if (m <= p) {
            throw new EstimationException("no degrees of freedom ({0} measurements, {1} parameters)",
                                          new Object[] { new Integer(m), new Integer(p)});
        }
        double[] errors = new double[problem.getAllParameters().length];
        final double c = Math.sqrt(getChiSquare(problem) / (m - p));
        double[][] covar = getCovariances(problem);
        for (int i = 0; i < errors.length; ++i) {
            errors[i] = Math.sqrt(covar[i][i]) * c;
        }
        return errors;
    }

// relevant test
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
  public void testNonInversible() throws EstimationException {

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
  public void testMoreEstimatedParametersSimple() throws EstimationException {

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
  public void testMoreEstimatedParametersUnsorted() throws EstimationException {
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
  public void testCircleFittingBadInit() throws EstimationException {
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

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testTrivial
  public void testTrivial() throws EstimationException {
    LinearProblem problem =
      new LinearProblem(new LinearMeasurement[] {
        new LinearMeasurement(new double[] {2},
                              new EstimatedParameter[] {
                                 new EstimatedParameter("p0", 0)
                              }, 3.0)
      });
    LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    try {
        estimator.guessParametersErrors(problem);
        fail("an exception should have been thrown");
    } catch (EstimationException ee) {
        
    } catch (Exception e) {
        fail("wrong exception caught");
    }
    assertEquals(1.5,
                 problem.getUnboundParameters()[0].getEstimate(),
                 1.0e-10);
   }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testQRColumnsPermutation
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

    LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    assertEquals(7.0, x[0].getEstimate(), 1.0e-10);
    assertEquals(3.0, x[1].getEstimate(), 1.0e-10);

  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testNoDependency
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
  LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
  estimator.estimate(problem);
  assertEquals(0, estimator.getRMS(problem), 1.0e-10);
  for (int i = 0; i < p.length; ++i) {
    assertEquals(0.55 * i, p[i].getEstimate(), 1.0e-10);
  }
}

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testOneSet
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

    LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    assertEquals(1.0, p[0].getEstimate(), 1.0e-10);
    assertEquals(2.0, p[1].getEstimate(), 1.0e-10);
    assertEquals(3.0, p[2].getEstimate(), 1.0e-10);

  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testTwoSets
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

    LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    assertEquals( 3.0, p[0].getEstimate(), 1.0e-10);
    assertEquals( 4.0, p[1].getEstimate(), 1.0e-10);
    assertEquals(-1.0, p[2].getEstimate(), 1.0e-10);
    assertEquals(-2.0, p[3].getEstimate(), 1.0e-10);
    assertEquals( 1.0 + epsilon, p[4].getEstimate(), 1.0e-10);
    assertEquals( 1.0 - epsilon, p[5].getEstimate(), 1.0e-10);

  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testNonInversible
  public void testNonInversible() throws EstimationException {

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

    LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
    double initialCost = estimator.getRMS(problem);
    estimator.estimate(problem);
    assertTrue(estimator.getRMS(problem) < initialCost);
    assertTrue(Math.sqrt(m.length) * estimator.getRMS(problem) > 0.6);
    try {
        estimator.getCovariances(problem);
        fail("an exception should have been thrown");
    } catch (EstimationException ee) {
        
    } catch (Exception e) {
        fail("wrong exception caught");
    }
   double dJ0 = 2 * (m[0].getResidual() * m[0].getPartial(p[0])
                    + m[1].getResidual() * m[1].getPartial(p[0])
                    + m[2].getResidual() * m[2].getPartial(p[0]));
    double dJ1 = 2 * (m[0].getResidual() * m[0].getPartial(p[1])
                    + m[1].getResidual() * m[1].getPartial(p[1]));
    double dJ2 = 2 * (m[0].getResidual() * m[0].getPartial(p[2])
                    + m[1].getResidual() * m[1].getPartial(p[2])
                    + m[2].getResidual() * m[2].getPartial(p[2]));
    assertEquals(0, dJ0, 1.0e-10);
    assertEquals(0, dJ1, 1.0e-10);
    assertEquals(0, dJ2, 1.0e-10);

  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testIllConditioned
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
    LevenbergMarquardtEstimator estimator1 = new LevenbergMarquardtEstimator();
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
    LevenbergMarquardtEstimator estimator2 = new LevenbergMarquardtEstimator();
    estimator2.estimate(problem2);
    assertEquals(0, estimator2.getRMS(problem2), 1.0e-10);
    assertEquals(-81.0, p[0].getEstimate(), 1.0e-8);
    assertEquals(137.0, p[1].getEstimate(), 1.0e-8);
    assertEquals(-34.0, p[2].getEstimate(), 1.0e-8);
    assertEquals( 22.0, p[3].getEstimate(), 1.0e-8);

  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testMoreEstimatedParametersSimple
  public void testMoreEstimatedParametersSimple() throws EstimationException {

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

    LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);

  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testMoreEstimatedParametersUnsorted
  public void testMoreEstimatedParametersUnsorted() throws EstimationException {
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

    LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    assertEquals(3.0, p[2].getEstimate(), 1.0e-10);
    assertEquals(4.0, p[3].getEstimate(), 1.0e-10);
    assertEquals(5.0, p[4].getEstimate(), 1.0e-10);
    assertEquals(6.0, p[5].getEstimate(), 1.0e-10);

  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testRedundantEquations
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

    LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
    estimator.estimate(problem);
    assertEquals(0, estimator.getRMS(problem), 1.0e-10);
    assertEquals(2.0, p[0].getEstimate(), 1.0e-10);
    assertEquals(1.0, p[1].getEstimate(), 1.0e-10);

  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testInconsistentEquations
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

    LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
    estimator.estimate(problem);
    assertTrue(estimator.getRMS(problem) > 0.1);

  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testControlParameters
  public void testControlParameters() throws EstimationException {
      Circle circle = new Circle(98.680, 47.345);
      circle.addPoint( 30.0,  68.0);
      circle.addPoint( 50.0,  -6.0);
      circle.addPoint(110.0, -20.0);
      circle.addPoint( 35.0,  15.0);
      circle.addPoint( 45.0,  97.0);
      checkEstimate(circle, 0.1, 10, 1.0e-14, 1.0e-16, 1.0e-10, false);
      checkEstimate(circle, 0.1, 10, 1.0e-15, 1.0e-17, 1.0e-10, true);
      checkEstimate(circle, 0.1,  5, 1.0e-15, 1.0e-16, 1.0e-10, true);
      circle.addPoint(300, -300);
      checkEstimate(circle, 0.1, 20, 1.0e-18, 1.0e-16, 1.0e-10, true);
  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testCircleFitting
  public void testCircleFitting() throws EstimationException {
      Circle circle = new Circle(98.680, 47.345);
      circle.addPoint( 30.0,  68.0);
      circle.addPoint( 50.0,  -6.0);
      circle.addPoint(110.0, -20.0);
      circle.addPoint( 35.0,  15.0);
      circle.addPoint( 45.0,  97.0);
      LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
      estimator.estimate(circle);
      assertTrue(estimator.getCostEvaluations() < 10);
      assertTrue(estimator.getJacobianEvaluations() < 10);
      double rms = estimator.getRMS(circle);
      assertEquals(1.768262623567235,  Math.sqrt(circle.getM()) * rms,  1.0e-10);
      assertEquals(69.96016176931406, circle.getRadius(), 1.0e-10);
      assertEquals(96.07590211815305, circle.getX(),      1.0e-10);
      assertEquals(48.13516790438953, circle.getY(),      1.0e-10);
      double[][] cov = estimator.getCovariances(circle);
      assertEquals(1.839, cov[0][0], 0.001);
      assertEquals(0.731, cov[0][1], 0.001);
      assertEquals(cov[0][1], cov[1][0], 1.0e-14);
      assertEquals(0.786, cov[1][1], 0.001);
      double[] errors = estimator.guessParametersErrors(circle);
      assertEquals(1.384, errors[0], 0.001);
      assertEquals(0.905, errors[1], 0.001);
  
      
      double cx = circle.getX();
      double cy = circle.getY();
      double  r = circle.getRadius();
      for (double d= 0; d < 2 * Math.PI; d += 0.01) {
          circle.addPoint(cx + r * Math.cos(d), cy + r * Math.sin(d));
      }
      estimator = new LevenbergMarquardtEstimator();
      estimator.estimate(circle);
      cov = estimator.getCovariances(circle);
      assertEquals(0.004, cov[0][0], 0.001);
      assertEquals(6.40e-7, cov[0][1], 1.0e-9);
      assertEquals(cov[0][1], cov[1][0], 1.0e-14);
      assertEquals(0.003, cov[1][1], 0.001);
      errors = estimator.guessParametersErrors(circle);
      assertEquals(0.004, errors[0], 0.001);
      assertEquals(0.004, errors[1], 0.001);

  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testCircleFittingBadInit
  public void testCircleFittingBadInit() throws EstimationException {
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
    LevenbergMarquardtEstimator estimator = new LevenbergMarquardtEstimator();
    estimator.estimate(circle);
    assertTrue(estimator.getCostEvaluations() < 15);
    assertTrue(estimator.getJacobianEvaluations() < 10);
    assertEquals( 0.030184491196225207, estimator.getRMS(circle), 1.0e-9);
    assertEquals( 0.2922350065939634,   circle.getRadius(), 1.0e-9);
    assertEquals(-0.15173845023862165,  circle.getX(),      1.0e-8);
    assertEquals( 0.20750021499570379,  circle.getY(),      1.0e-8);
  }

// org.apache.commons.math.estimation.LevenbergMarquardtEstimatorTest::testMath199
  public void testMath199() {
      try {
          QuadraticProblem problem = new QuadraticProblem();
          problem.addPoint (0, -3.182591015485607, 0.0);
          problem.addPoint (1, -2.5581184967730577, 4.4E-323);
          problem.addPoint (2, -2.1488478161387325, 1.0);
          problem.addPoint (3, -1.9122489313410047, 4.4E-323);
          problem.addPoint (4, 1.7785661310051026, 0.0);
          new LevenbergMarquardtEstimator().estimate(problem);
          fail("an exception should have been thrown");
      } catch (EstimationException ee) {
          
      }

  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackLinearFullRank
  public void testMinpackLinearFullRank()
    throws EstimationException {
    minpackTest(new LinearFullRankFunction(10, 5, 1.0,
                                           5.0, 2.23606797749979), false);
    minpackTest(new LinearFullRankFunction(50, 5, 1.0,
                                           8.06225774829855, 6.70820393249937), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackLinearRank1
  public void testMinpackLinearRank1()
    throws EstimationException {
    minpackTest(new LinearRank1Function(10, 5, 1.0,
                                        291.521868819476, 1.4638501094228), false);
    minpackTest(new LinearRank1Function(50, 5, 1.0,
                                        3101.60039334535, 3.48263016573496), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackLinearRank1ZeroColsAndRows
  public void testMinpackLinearRank1ZeroColsAndRows()
    throws EstimationException {
    minpackTest(new LinearRank1ZeroColsAndRowsFunction(10, 5, 1.0), false);
    minpackTest(new LinearRank1ZeroColsAndRowsFunction(50, 5, 1.0), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackRosenbrok
  public void testMinpackRosenbrok()
    throws EstimationException {
    minpackTest(new RosenbrockFunction(new double[] { -1.2, 1.0 },
                                       Math.sqrt(24.2)), false);
    minpackTest(new RosenbrockFunction(new double[] { -12.0, 10.0 },
                                       Math.sqrt(1795769.0)), false);
    minpackTest(new RosenbrockFunction(new double[] { -120.0, 100.0 },
                                       11.0 * Math.sqrt(169000121.0)), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackHelicalValley
  public void testMinpackHelicalValley()
    throws EstimationException {
    minpackTest(new HelicalValleyFunction(new double[] { -1.0, 0.0, 0.0 },
                                          50.0), false);
    minpackTest(new HelicalValleyFunction(new double[] { -10.0, 0.0, 0.0 },
                                          102.95630140987), false);
    minpackTest(new HelicalValleyFunction(new double[] { -100.0, 0.0, 0.0},
                                          991.261822123701), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackPowellSingular
  public void testMinpackPowellSingular()
    throws EstimationException {
    minpackTest(new PowellSingularFunction(new double[] { 3.0, -1.0, 0.0, 1.0 },
                                           14.6628782986152), false);
    minpackTest(new PowellSingularFunction(new double[] { 30.0, -10.0, 0.0, 10.0 },
                                           1270.9838708654), false);
    minpackTest(new PowellSingularFunction(new double[] { 300.0, -100.0, 0.0, 100.0 },
                                           126887.903284750), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackFreudensteinRoth
  public void testMinpackFreudensteinRoth()
    throws EstimationException {
    minpackTest(new FreudensteinRothFunction(new double[] { 0.5, -2.0 },
                                             20.0124960961895, 6.99887517584575,
                                             new double[] {
                                               11.4124844654993,
                                               -0.896827913731509
                                             }), false);
    minpackTest(new FreudensteinRothFunction(new double[] { 5.0, -20.0 },
                                             12432.833948863, 6.9988751744895,
                                             new double[] {
                                               11.4130046614746,
                                               -0.896796038685958
                                             }), false);
    minpackTest(new FreudensteinRothFunction(new double[] { 50.0, -200.0 },
                                             11426454.595762, 6.99887517242903,
                                             new double[] {
                                               11.4127817857886,
                                               -0.89680510749204
                                             }), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackBard
  public void testMinpackBard()
    throws EstimationException {
    minpackTest(new BardFunction(1.0, 6.45613629515967, 0.0906359603390466,
                                 new double[] {
                                   0.0824105765758334,
                                   1.1330366534715,
                                   2.34369463894115
                                 }), false);
    minpackTest(new BardFunction(10.0, 36.1418531596785, 4.17476870138539,
                                 new double[] {
                                   0.840666673818329,
                                   -158848033.259565,
                                   -164378671.653535
                                 }), false);
    minpackTest(new BardFunction(100.0, 384.114678637399, 4.17476870135969,
                                 new double[] {
                                   0.840666673867645,
                                   -158946167.205518,
                                   -164464906.857771
                                 }), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackKowalikOsborne
  public void testMinpackKowalikOsborne()
    throws EstimationException {
    minpackTest(new KowalikOsborneFunction(new double[] { 0.25, 0.39, 0.415, 0.39 },
                                           0.0728915102882945,
                                           0.017535837721129,
                                           new double[] {
                                             0.192807810476249,
                                             0.191262653354071,
                                             0.123052801046931,
                                             0.136053221150517
                                           }), false);
    minpackTest(new KowalikOsborneFunction(new double[] { 2.5, 3.9, 4.15, 3.9 },
                                           2.97937007555202,
                                           0.032052192917937,
                                           new double[] {
                                             728675.473768287,
                                             -14.0758803129393,
                                             -32977797.7841797,
                                             -20571594.1977912
                                           }), false);
    minpackTest(new KowalikOsborneFunction(new double[] { 25.0, 39.0, 41.5, 39.0 },
                                           29.9590617016037,
                                           0.0175364017658228,
                                           new double[] {
                                             0.192948328597594,
                                             0.188053165007911,
                                             0.122430604321144,
                                             0.134575665392506
                                           }), true);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackMeyer
  public void testMinpackMeyer() {}

// org.apache.commons.math.estimation.MinpackTest::testMinpackWatson
  public void testMinpackWatson()
    throws EstimationException {
  
    minpackTest(new WatsonFunction(6, 0.0,
                                   5.47722557505166, 0.0478295939097601,
                                   new double[] {
                                     -0.0157249615083782, 1.01243488232965,
                                     -0.232991722387673,  1.26043101102818,
                                     -1.51373031394421,   0.99299727291842
                                   }), false);
    minpackTest(new WatsonFunction(6, 10.0,
                                   6433.12578950026, 0.0478295939096951,
                                   new double[] {
                                     -0.0157251901386677, 1.01243485860105,
                                     -0.232991545843829,  1.26042932089163,
                                     -1.51372776706575,   0.99299573426328
                                   }), false);
    minpackTest(new WatsonFunction(6, 100.0,
                                   674256.040605213, 0.047829593911544,
                                   new double[] {
                                    -0.0157247019712586, 1.01243490925658,
                                    -0.232991922761641,  1.26043292929555,
                                    -1.51373320452707,   0.99299901922322
                                   }), false);

    minpackTest(new WatsonFunction(9, 0.0,
                                   5.47722557505166, 0.00118311459212420,
                                   new double[] {
                                    -0.153070644166722e-4, 0.999789703934597,
                                     0.0147639634910978,   0.146342330145992,
                                     1.00082109454817,    -2.61773112070507,
                                     4.10440313943354,    -3.14361226236241,
                                     1.05262640378759
                                   }), false);
    minpackTest(new WatsonFunction(9, 10.0,
                                   12088.127069307, 0.00118311459212513,
                                   new double[] {
                                   -0.153071334849279e-4, 0.999789703941234,
                                    0.0147639629786217,   0.146342334818836,
                                    1.00082107321386,    -2.61773107084722,
                                    4.10440307655564,    -3.14361222178686,
                                    1.05262639322589
                                   }), false);
    minpackTest(new WatsonFunction(9, 100.0,
                                   1269109.29043834, 0.00118311459212384,
                                   new double[] {
                                    -0.153069523352176e-4, 0.999789703958371,
                                     0.0147639625185392,   0.146342341096326,
                                     1.00082104729164,    -2.61773101573645,
                                     4.10440301427286,    -3.14361218602503,
                                     1.05262638516774
                                   }), false);

    minpackTest(new WatsonFunction(12, 0.0,
                                   5.47722557505166, 0.217310402535861e-4,
                                   new double[] {
                                    -0.660266001396382e-8, 1.00000164411833,
                                    -0.000563932146980154, 0.347820540050756,
                                    -0.156731500244233,    1.05281515825593,
                                    -3.24727109519451,     7.2884347837505,
                                   -10.271848098614,       9.07411353715783,
                                    -4.54137541918194,     1.01201187975044
                                   }), false);
    minpackTest(new WatsonFunction(12, 10.0,
                                   19220.7589790951, 0.217310402518509e-4,
                                   new double[] {
                                    -0.663710223017410e-8, 1.00000164411787,
                                    -0.000563932208347327, 0.347820540486998,
                                    -0.156731503955652,    1.05281517654573,
                                    -3.2472711515214,      7.28843489430665,
                                   -10.2718482369638,      9.07411364383733,
                                    -4.54137546533666,     1.01201188830857
                                   }), false);
    minpackTest(new WatsonFunction(12, 100.0,
                                   2018918.04462367, 0.217310402539845e-4,
                                   new double[] {
                                    -0.663806046485249e-8, 1.00000164411786,
                                    -0.000563932210324959, 0.347820540503588,
                                    -0.156731504091375,    1.05281517718031,
                                    -3.24727115337025,     7.28843489775302,
                                   -10.2718482410813,      9.07411364688464,
                                    -4.54137546660822,     1.0120118885369
                                   }), false);

  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackBox3Dimensional
  public void testMinpackBox3Dimensional()
  throws EstimationException {
    minpackTest(new Box3DimensionalFunction(10, new double[] { 0.0, 10.0, 20.0 },
                                            32.1115837449572), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackJennrichSampson
  public void testMinpackJennrichSampson()
    throws EstimationException {
    minpackTest(new JennrichSampsonFunction(10, new double[] { 0.3, 0.4 },
                                            64.5856498144943, 11.1517793413499,
                                            new double[] {
                                             0.257819926636811, 0.257829976764542
                                            }), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackBrownDennis
  public void testMinpackBrownDennis()
    throws EstimationException {
    minpackTest(new BrownDennisFunction(20,
                                        new double[] { 25.0, 5.0, -5.0, -1.0 },
                                        2815.43839161816, 292.954288244866,
                                        new double[] {
                                         -11.59125141003, 13.2024883984741,
                                         -0.403574643314272, 0.236736269844604
                                        }), false);
    minpackTest(new BrownDennisFunction(20,
                                        new double[] { 250.0, 50.0, -50.0, -10.0 },
                                        555073.354173069, 292.954270581415,
                                        new double[] {
                                         -11.5959274272203, 13.2041866926242,
                                         -0.403417362841545, 0.236771143410386
                                       }), false);
    minpackTest(new BrownDennisFunction(20,
                                        new double[] { 2500.0, 500.0, -500.0, -100.0 },
                                        61211252.2338581, 292.954306151134,
                                        new double[] {
                                         -11.5902596937374, 13.2020628854665,
                                         -0.403688070279258, 0.236665033746463
                                        }), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackChebyquad
  public void testMinpackChebyquad()
    throws EstimationException {
    minpackTest(new ChebyquadFunction(1, 8, 1.0,
                                      1.88623796907732, 1.88623796907732,
                                      new double[] { 0.5 }), false);
    minpackTest(new ChebyquadFunction(1, 8, 10.0,
                                      5383344372.34005, 1.88424820499951,
                                      new double[] { 0.9817314924684 }), false);
    minpackTest(new ChebyquadFunction(1, 8, 100.0,
                                      0.118088726698392e19, 1.88424820499347,
                                      new double[] { 0.9817314852934 }), false);
    minpackTest(new ChebyquadFunction(8, 8, 1.0,
                                      0.196513862833975, 0.0593032355046727,
                                      new double[] {
                                        0.0431536648587336, 0.193091637843267,
                                        0.266328593812698,  0.499999334628884,
                                        0.500000665371116,  0.733671406187302,
                                        0.806908362156733,  0.956846335141266
                                      }), false);
    minpackTest(new ChebyquadFunction(9, 9, 1.0,
                                      0.16994993465202, 0.0,
                                      new double[] {
                                        0.0442053461357828, 0.199490672309881,
                                        0.23561910847106,   0.416046907892598,
                                        0.5,                0.583953092107402,
                                        0.764380891528940,  0.800509327690119,
                                        0.955794653864217
                                      }), false);
    minpackTest(new ChebyquadFunction(10, 10, 1.0,
                                      0.183747831178711, 0.0806471004038253,
                                      new double[] {
                                        0.0596202671753563, 0.166708783805937,
                                        0.239171018813509,  0.398885290346268,
                                        0.398883667870681,  0.601116332129320,
                                        0.60111470965373,   0.760828981186491,
                                        0.833291216194063,  0.940379732824644
                                      }), false);
  }

// org.apache.commons.math.estimation.MinpackTest::testMinpackBrownAlmostLinear
  public void testMinpackBrownAlmostLinear()
    throws EstimationException {
    minpackTest(new BrownAlmostLinearFunction(10, 0.5,
                                              16.5302162063499, 0.0,
                                              new double[] {
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 0.979430303349862,
                                                0.979430303349862, 1.20569696650138
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(10, 5.0,
                                              9765624.00089211, 0.0,
                                              new double[] {
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 0.979430303349865,
                                               0.979430303349865, 1.20569696650135
                                              }), false);  
    minpackTest(new BrownAlmostLinearFunction(10, 50.0,
                                              0.9765625e17, 0.0,
                                              new double[] {
                                                1.0, 1.0, 1.0, 1.0, 1.0,
                                                1.0, 1.0, 1.0, 1.0, 1.0
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(30, 0.5,
                                              83.476044467848, 0.0,
                                              new double[] {
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 0.997754216442807,
                                                0.997754216442807, 1.06737350671578
                                              }), false);
    minpackTest(new BrownAlmostLinearFunction(40, 0.5,
                                              128.026364472323, 0.0,
                                              new double[] {
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                1.00000000000002, 1.00000000000002,
                                                0.999999999999121
                                              }), false);
    }

// org.apache.commons.math.estimation.MinpackTest::testMinpackOsborne1
  public void testMinpackOsborne1()
    throws EstimationException {
      minpackTest(new Osborne1Function(new double[] { 0.5, 1.5, -1.0, 0.01, 0.02, },
                                       0.937564021037838, 0.00739249260904843,
                                       new double[] {
                                         0.375410049244025, 1.93584654543108,
                                        -1.46468676748716, 0.0128675339110439,
                                         0.0221227011813076
                                       }), false);
    }

// org.apache.commons.math.estimation.MinpackTest::testMinpackOsborne2
  public void testMinpackOsborne2()
    throws EstimationException {
      
    minpackTest(new Osborne2Function(new double[] {
                                       1.3, 0.65, 0.65, 0.7, 0.6,
                                       3.0, 5.0, 7.0, 2.0, 4.5, 5.5
                                     },
                                     1.44686540984712, 0.20034404483314,
                                     new double[] {
                                       1.30997663810096,  0.43155248076,
                                       0.633661261602859, 0.599428560991695,
                                       0.754179768272449, 0.904300082378518,
                                       1.36579949521007, 4.82373199748107,
                                       2.39868475104871, 4.56887554791452,
                                       5.67534206273052
                                     }), false);
  }
