// buggy code
    public BigDecimal[] operate(BigDecimal[] v) throws IllegalArgumentException {
        if (v.length != this.getColumnDimension()) {
            throw new IllegalArgumentException("vector has wrong length");
        }
        final int nRows = this.getRowDimension();
        final int nCols = this.getColumnDimension();
        final BigDecimal[] out = new BigDecimal[v.length];
        for (int row = 0; row < nRows; row++) {
            BigDecimal sum = ZERO;
            for (int i = 0; i < nCols; i++) {
                sum = sum.add(data[row][i].multiply(v[i]));
            }
            out[row] = sum;
        }
        return out;
    }

    public double[] operate(double[] v) throws IllegalArgumentException {
        final int nRows = this.getRowDimension();
        final int nCols = this.getColumnDimension();
        if (v.length != nCols) {
            throw new IllegalArgumentException("vector has wrong length");
        }
        final double[] out = new double[v.length];
        for (int row = 0; row < nRows; row++) {
            final double[] dataRow = data[row];
            double sum = 0;
            for (int i = 0; i < nCols; i++) {
                sum += dataRow[i] * v[i];
            }
            out[row] = sum;
        }
        return out;
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

// org.apache.commons.math.linear.BigMatrixImplTest::testDimensions
    public void testDimensions() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrixImpl m2 = new BigMatrixImpl(testData2);
        assertEquals("testData row dimension",3,m.getRowDimension());
        assertEquals("testData column dimension",3,m.getColumnDimension());
        assertTrue("testData is square",m.isSquare());
        assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testCopyFunctions
    public void testCopyFunctions() {
        BigMatrixImpl m1 = new BigMatrixImpl(testData);
        BigMatrixImpl m2 = new BigMatrixImpl(m1.getData());
        assertEquals(m2,m1);
        BigMatrixImpl m3 = new BigMatrixImpl(testData);
        BigMatrixImpl m4 = new BigMatrixImpl(m3.getData(), false);
        assertEquals(m4,m3);
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testConstructors
    public void testConstructors() {
        BigMatrix m1 = new BigMatrixImpl(testData);
        BigMatrix m2 = new BigMatrixImpl(testDataString);
        BigMatrix m3 = new BigMatrixImpl(asBigDecimal(testData));
        BigMatrix m4 = new BigMatrixImpl(asBigDecimal(testData), true);
        BigMatrix m5 = new BigMatrixImpl(asBigDecimal(testData), false);
        assertClose("double, string", m1, m2, Double.MIN_VALUE);
        assertClose("double, BigDecimal", m1, m3, Double.MIN_VALUE);
        assertClose("string, BigDecimal", m2, m3, Double.MIN_VALUE);
        assertClose("double, BigDecimal/true", m1, m4, Double.MIN_VALUE);
        assertClose("double, BigDecimal/false", m1, m5, Double.MIN_VALUE);
        try {
            new BigMatrixImpl(new String[][] {{"0", "hello", "1"}});
            fail("Expecting NumberFormatException");
        } catch (NumberFormatException ex) {
            
        }
        try {
            new BigMatrixImpl(new String[][] {});
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            new BigMatrixImpl(new String[][] {{},{}});
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            new BigMatrixImpl(new String[][] {{"a", "b"},{"c"}});
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }

        try {
            new BigMatrixImpl(0, 1);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            new BigMatrixImpl(1, 0);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testAdd
    public void testAdd() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrixImpl mInv = new BigMatrixImpl(testDataInv);
        BigMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = asDouble(mPlusMInv.getData());
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                assertEquals("sum entry entry",
                    testDataPlusInv[row][col],sumEntries[row][col],
                        entryTolerance);
            }
        }    
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testAddFail
    public void testAddFail() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrixImpl m2 = new BigMatrixImpl(testData2);
        try {
            m.add(m2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testNorm
    public void testNorm() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrixImpl m2 = new BigMatrixImpl(testData2);
        assertEquals("testData norm",14d,m.getNorm().doubleValue(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm().doubleValue(),entryTolerance);
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testPlusMinus
    public void testPlusMinus() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrixImpl m2 = new BigMatrixImpl(testDataInv);
        assertClose("m-n = m + -n",m.subtract(m2),
            m2.scalarMultiply(new BigDecimal(-1d)).add(m),entryTolerance);
        try {
            m.subtract(new BigMatrixImpl(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testMultiply
     public void testMultiply() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrixImpl mInv = new BigMatrixImpl(testDataInv);
        BigMatrixImpl identity = new BigMatrixImpl(id);
        BigMatrixImpl m2 = new BigMatrixImpl(testData2);
        assertClose("inverse multiply",m.multiply(mInv),
            identity,entryTolerance);
        assertClose("inverse multiply",mInv.multiply(m),
            identity,entryTolerance);
        assertClose("identity multiply",m.multiply(identity),
            m,entryTolerance);
        assertClose("identity multiply",identity.multiply(mInv),
            mInv,entryTolerance);
        assertClose("identity multiply",m2.multiply(identity),
            m2,entryTolerance); 
        try {
            m.multiply(new BigMatrixImpl(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testMultiply2
    public void testMultiply2() { 
       BigMatrix m3 = new BigMatrixImpl(d3);
       BigMatrix m4 = new BigMatrixImpl(d4);
       BigMatrix m5 = new BigMatrixImpl(d5);
       assertClose("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.BigMatrixImplTest::testIsSingular
    public void testIsSingular() {
        BigMatrixImpl m = new BigMatrixImpl(singular);
        assertTrue("singular",m.isSingular());
        m = new BigMatrixImpl(bigSingular);
        assertTrue("big singular",m.isSingular());
        m = new BigMatrixImpl(id);
        assertTrue("identity nonsingular",!m.isSingular());
        m = new BigMatrixImpl(testData);
        assertTrue("testData nonsingular",!m.isSingular());
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testInverse
    public void testInverse() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrix mInv = new BigMatrixImpl(testDataInv);
        assertClose("inverse",mInv,m.inverse(),normTolerance);
        assertClose("inverse^2",m,m.inverse().inverse(),10E-12);
        
        
        m = new BigMatrixImpl(testData2);
        try {
            m.inverse();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
        
        
        m = new BigMatrixImpl(singular);
        try {
            m.inverse();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testSolve
    public void testSolve() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrix mInv = new BigMatrixImpl(testDataInv);
        
        assertClose("inverse-operate",
                    asDouble(mInv.operate(asBigDecimal(testVector))),
                    asDouble(m.solve(asBigDecimal(testVector))),
                    normTolerance);
        try {
            asDouble(m.solve(asBigDecimal(testVector2)));
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }       
        BigMatrix bs = new BigMatrixImpl(bigSingular);
        try {
            bs.solve(bs);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            ;
        }
        try {
            m.solve(bs);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
        try {
            new BigMatrixImpl(testData2).solve(bs);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        } 
        try {
            (new BigMatrixImpl(testData2)).luDecompose();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            ;
        }  
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testDeterminant
    public void testDeterminant() {       
        BigMatrix m = new BigMatrixImpl(bigSingular);
        assertEquals("singular determinant",0,m.getDeterminant().doubleValue(),0);
        m = new BigMatrixImpl(detData);
        assertEquals("nonsingular test",-3d,m.getDeterminant().doubleValue(),normTolerance);
        
        
        m = new BigMatrixImpl(detData2);
        assertEquals("nonsingular R test 1",-2d,m.getDeterminant().doubleValue(),normTolerance);
        m = new BigMatrixImpl(testData);
        assertEquals("nonsingular  R test 2",-1d,m.getDeterminant().doubleValue(),normTolerance);

        try {
            new BigMatrixImpl(testData2).getDeterminant().doubleValue();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testTrace
    public void testTrace() {
        BigMatrix m = new BigMatrixImpl(id);
        assertEquals("identity trace",3d,m.getTrace().doubleValue(),entryTolerance);
        m = new BigMatrixImpl(testData2);
        try {
            m.getTrace().doubleValue();
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testScalarAdd
    public void testScalarAdd() {
        BigMatrix m = new BigMatrixImpl(testData);
        assertClose("scalar add",new BigMatrixImpl(testDataPlus2),
            m.scalarAdd(new BigDecimal(2d)),entryTolerance);
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testOperate
    public void testOperate() {
        BigMatrix m = new BigMatrixImpl(id);
        double[] x = asDouble(m.operate(asBigDecimal(testVector)));
        assertClose("identity operate",testVector,x,entryTolerance);
        m = new BigMatrixImpl(bigSingular);
        try {
            asDouble(m.operate(asBigDecimal(testVector)));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testMath209
    public void testMath209() {
        BigMatrix a = new BigMatrixImpl(new BigDecimal[][] {
                { new BigDecimal(1), new BigDecimal(2) },
                { new BigDecimal(3), new BigDecimal(4) },
                { new BigDecimal(5), new BigDecimal(6) }
        }, false);
        BigDecimal[] b = a.operate(new BigDecimal[] { new BigDecimal(1), new BigDecimal(1) });
        assertEquals(a.getRowDimension(), b.length);
        assertEquals( 3.0, b[0].doubleValue(), 1.0e-12);
        assertEquals( 7.0, b[1].doubleValue(), 1.0e-12);
        assertEquals(11.0, b[2].doubleValue(), 1.0e-12);
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testTranspose
    public void testTranspose() {
        BigMatrix m = new BigMatrixImpl(testData);
        assertClose("inverse-transpose",m.inverse().transpose(),
            m.transpose().inverse(),normTolerance);
        m = new BigMatrixImpl(testData2);
        BigMatrix mt = new BigMatrixImpl(testData2T);
        assertClose("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testPremultiplyVector
    public void testPremultiplyVector() {
        BigMatrix m = new BigMatrixImpl(testData);
        assertClose("premultiply",asDouble(m.preMultiply(asBigDecimal(testVector))),preMultTest,normTolerance);
        m = new BigMatrixImpl(bigSingular);
        try {
            m.preMultiply(asBigDecimal(testVector));
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testPremultiply
    public void testPremultiply() {
        BigMatrix m3 = new BigMatrixImpl(d3);
        BigMatrix m4 = new BigMatrixImpl(d4);
        BigMatrix m5 = new BigMatrixImpl(d5);
        assertClose("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);
        
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrixImpl mInv = new BigMatrixImpl(testDataInv);
        BigMatrixImpl identity = new BigMatrixImpl(id);
        new BigMatrixImpl(testData2);
        assertClose("inverse multiply",m.preMultiply(mInv),
                identity,entryTolerance);
        assertClose("inverse multiply",mInv.preMultiply(m),
                identity,entryTolerance);
        assertClose("identity multiply",m.preMultiply(identity),
                m,entryTolerance);
        assertClose("identity multiply",identity.preMultiply(mInv),
                mInv,entryTolerance);
        try {
            m.preMultiply(new BigMatrixImpl(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testGetVectors
    public void testGetVectors() {
        BigMatrix m = new BigMatrixImpl(testData);
        assertClose("get row",m.getRowAsDoubleArray(0),testDataRow1,entryTolerance);
        assertClose("get col",m.getColumnAsDoubleArray(2),testDataCol3,entryTolerance);
        try {
            m.getRowAsDoubleArray(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            ;
        }
        try {
            m.getColumnAsDoubleArray(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testLUDecomposition
    public void testLUDecomposition() throws Exception {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrix lu = m.getLUMatrix();
        assertClose("LU decomposition", lu, (BigMatrix) new BigMatrixImpl(testDataLU), normTolerance);
        verifyDecomposition(m, lu);
        m = new BigMatrixImpl(luData);
        lu = m.getLUMatrix();
        assertClose("LU decomposition", lu, (BigMatrix) new BigMatrixImpl(luDataLUDecomposition), normTolerance);
        verifyDecomposition(m, lu);
        m = new BigMatrixImpl(testDataMinus);
        lu = m.getLUMatrix();
        verifyDecomposition(m, lu);
        m = new BigMatrixImpl(id);
        lu = m.getLUMatrix();
        verifyDecomposition(m, lu);
        try {
            m = new BigMatrixImpl(bigSingular); 
            lu = m.getLUMatrix();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
        try {
            m = new BigMatrixImpl(testData2);  
            lu = m.getLUMatrix();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testSubMatrix
    public void testSubMatrix() {
        BigMatrix m = new BigMatrixImpl(subTestData);
        BigMatrix mRows23Cols00 = new BigMatrixImpl(subRows23Cols00);
        BigMatrix mRows00Cols33 = new BigMatrixImpl(subRows00Cols33);
        BigMatrix mRows01Cols23 = new BigMatrixImpl(subRows01Cols23);
        BigMatrix mRows02Cols13 = new BigMatrixImpl(subRows02Cols13);
        BigMatrix mRows03Cols12 = new BigMatrixImpl(subRows03Cols12);
        BigMatrix mRows03Cols123 = new BigMatrixImpl(subRows03Cols123);
        BigMatrix mRows20Cols123 = new BigMatrixImpl(subRows20Cols123);
        BigMatrix mRows31Cols31 = new BigMatrixImpl(subRows31Cols31);
        assertEquals("Rows23Cols00", mRows23Cols00, 
                m.getSubMatrix(2 , 3 , 0, 0));
        assertEquals("Rows00Cols33", mRows00Cols33, 
                m.getSubMatrix(0 , 0 , 3, 3));
        assertEquals("Rows01Cols23", mRows01Cols23,
                m.getSubMatrix(0 , 1 , 2, 3));   
        assertEquals("Rows02Cols13", mRows02Cols13,
                m.getSubMatrix(new int[] {0,2}, new int[] {1,3}));  
        assertEquals("Rows03Cols12", mRows03Cols12,
                m.getSubMatrix(new int[] {0,3}, new int[] {1,2}));  
        assertEquals("Rows03Cols123", mRows03Cols123,
                m.getSubMatrix(new int[] {0,3}, new int[] {1,2,3})); 
        assertEquals("Rows20Cols123", mRows20Cols123,
                m.getSubMatrix(new int[] {2,0}, new int[] {1,2,3})); 
        assertEquals("Rows31Cols31", mRows31Cols31,
                m.getSubMatrix(new int[] {3,1}, new int[] {3,1})); 
        assertEquals("Rows31Cols31", mRows31Cols31,
                m.getSubMatrix(new int[] {3,1}, new int[] {3,1})); 
        
        try {
            m.getSubMatrix(1,0,2,4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(-1,1,2,2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1,0,2,2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1,0,2,4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] {0});
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {0}, new int[] {4});
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        BigMatrix m = new BigMatrixImpl(subTestData);
        BigMatrix mColumn1 = new BigMatrixImpl(subColumn1);
        BigMatrix mColumn3 = new BigMatrixImpl(subColumn3);
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

// org.apache.commons.math.linear.BigMatrixImplTest::testGetRowMatrix
    public void testGetRowMatrix() {
        BigMatrix m = new BigMatrixImpl(subTestData);
        BigMatrix mRow0 = new BigMatrixImpl(subRow0);
        BigMatrix mRow3 = new BigMatrixImpl(subRow3);
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

// org.apache.commons.math.linear.BigMatrixImplTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        BigMatrixImpl m1 = (BigMatrixImpl) m.copy();
        BigMatrixImpl mt = (BigMatrixImpl) m.transpose();
        assertTrue(m.hashCode() != mt.hashCode());
        assertEquals(m.hashCode(), m1.hashCode());
        assertEquals(m, m);
        assertEquals(m, m1);
        assertFalse(m.equals(null));
        assertFalse(m.equals(mt));
        assertFalse(m.equals(new BigMatrixImpl(bigSingular)));
        
        m = new BigMatrixImpl(new String[][] {{"2.0"}});
        m1 = new BigMatrixImpl(new String[][] {{"2.00"}});
        assertTrue(m.hashCode() != m1.hashCode());
        assertFalse(m.equals(m1));
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testToString
    public void testToString() {
        BigMatrixImpl m = new BigMatrixImpl(testData);
        assertEquals("BigMatrixImpl{{1,2,3},{2,5,3},{1,0,8}}",
                m.toString());
        m = new BigMatrixImpl();
        assertEquals("BigMatrixImpl{}",
                m.toString());
    }

// org.apache.commons.math.linear.BigMatrixImplTest::testSetSubMatrix
    public void testSetSubMatrix() throws Exception {
        BigDecimal[][] detData3 = 
            MatrixUtils.createBigMatrix(detData2).getData();
        BigMatrixImpl m = new BigMatrixImpl(testData);
        m.setSubMatrix(detData3,1,1);
        BigMatrix expected = MatrixUtils.createBigMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);  
        
        m.setSubMatrix(detData3,0,0);
        expected = MatrixUtils.createBigMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        assertEquals(expected, m);  
        
        BigDecimal[][] testDataPlus3 = 
            MatrixUtils.createBigMatrix(testDataPlus2).getData();
        m.setSubMatrix(testDataPlus3,0,0);      
        expected = MatrixUtils.createBigMatrix
        (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        assertEquals(expected, m);   
        
        
        BigMatrixImpl matrix = (BigMatrixImpl) MatrixUtils.createBigMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1 , 2}});
        matrix.setSubMatrix(new BigDecimal[][] {{new BigDecimal(3),
            new BigDecimal(4)}, {new BigDecimal(5), new BigDecimal(6)}}, 1, 1);
        expected = MatrixUtils.createBigMatrix
            (new BigDecimal[][] {{new BigDecimal(1), new BigDecimal(2),
             new BigDecimal(3), new BigDecimal(4)}, {new BigDecimal(5),
             new BigDecimal(3), new BigDecimal(4), new BigDecimal(8)},
             {new BigDecimal(9), new BigDecimal(5) , new BigDecimal(6),
              new BigDecimal(2)}});
        assertEquals(expected, matrix);   
        
        
        try {  
            m.setSubMatrix(matrix.getData(),1,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        
        
        try {
            m.setSubMatrix(null,1,1);
            fail("expecting NullPointerException");
        } catch (NullPointerException e) {
            
        }
        
        
        try {
            m.setSubMatrix(new BigDecimal[][] {{new BigDecimal(1)},
                    {new BigDecimal(2), new BigDecimal(3)}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        
        
        try {
            m.setSubMatrix(new BigDecimal[][] {{}}, 0, 0);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRealMatrix
    public void testCreateRealMatrix() {
        assertEquals(new RealMatrixImpl(testData), 
                MatrixUtils.createRealMatrix(testData));
        assertEquals(new RealMatrixImpl(testData, false), 
                MatrixUtils.createRealMatrix(testData, true));
        assertEquals(new RealMatrixImpl(testData, true), 
                MatrixUtils.createRealMatrix(testData, false));
        try {
            MatrixUtils.createRealMatrix(new double[][] {{1}, {1,2}});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        } 
        try {
            MatrixUtils.createRealMatrix(new double[][] {{}, {}});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRealMatrix(null);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        } 
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateBigMatrix
    public void testCreateBigMatrix() {
        assertEquals(new BigMatrixImpl(testData), 
                MatrixUtils.createBigMatrix(testData));
        assertEquals(new BigMatrixImpl(BigMatrixImplTest.asBigDecimal(testData), true), 
                MatrixUtils.createBigMatrix(BigMatrixImplTest.asBigDecimal(testData), false));
        assertEquals(new BigMatrixImpl(BigMatrixImplTest.asBigDecimal(testData), false), 
                MatrixUtils.createBigMatrix(BigMatrixImplTest.asBigDecimal(testData), true));
        assertEquals(new BigMatrixImpl(bigColMatrix), 
                MatrixUtils.createBigMatrix(bigColMatrix));
        assertEquals(new BigMatrixImpl(stringColMatrix), 
                MatrixUtils.createBigMatrix(stringColMatrix));
        try {
            MatrixUtils.createBigMatrix(new double[][] {{1}, {1,2}});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        } 
        try {
            MatrixUtils.createBigMatrix(new double[][] {{}, {}});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createBigMatrix(nullMatrix);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        } 
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRowRealMatrix
    public void testCreateRowRealMatrix() {
        assertEquals((RealMatrixImpl) MatrixUtils.createRowRealMatrix(row),
               new RealMatrixImpl(rowMatrix));
        try {
            MatrixUtils.createRowRealMatrix(new double[] {});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRowRealMatrix(null);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        } 
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateRowBigMatrix
    public void testCreateRowBigMatrix() {
        assertEquals((BigMatrixImpl) MatrixUtils.createRowBigMatrix(row),
                new BigMatrixImpl(rowMatrix));
        assertEquals((BigMatrixImpl) MatrixUtils.createRowBigMatrix(bigRow),
                new BigMatrixImpl(bigRowMatrix));
        assertEquals((BigMatrixImpl) MatrixUtils.createRowBigMatrix(stringRow),
                new BigMatrixImpl(stringRowMatrix));
        try {
            MatrixUtils.createRowBigMatrix(new double[] {});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createRowBigMatrix(nullDoubleArray);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        } 
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateColumnRealMatrix
    public void testCreateColumnRealMatrix() {
        assertEquals((RealMatrixImpl) MatrixUtils.createColumnRealMatrix(col),
                new RealMatrixImpl(colMatrix));
        try {
            MatrixUtils.createColumnRealMatrix(new double[] {});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnRealMatrix(null);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        } 
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateColumnBigMatrix
    public void testCreateColumnBigMatrix() {
        assertEquals((BigMatrixImpl) MatrixUtils.createColumnBigMatrix(col),
                new BigMatrixImpl(colMatrix));
        assertEquals((BigMatrixImpl) MatrixUtils.createColumnBigMatrix(bigCol),
                new BigMatrixImpl(bigColMatrix));
        assertEquals((BigMatrixImpl) MatrixUtils.createColumnBigMatrix(stringCol),
                new BigMatrixImpl(stringColMatrix));   
       
        try {
            MatrixUtils.createColumnBigMatrix(new double[] {});  
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            MatrixUtils.createColumnBigMatrix(nullDoubleArray);  
            fail("Expecting NullPointerException");
        } catch (NullPointerException ex) {
            
        } 
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateIdentityMatrix
    public void testCreateIdentityMatrix() {
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(3));
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(2));
        checkIdentityMatrix(MatrixUtils.createRealIdentityMatrix(1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.MatrixUtilsTest::testCreateBigIdentityMatrix
    public void testCreateBigIdentityMatrix() {
        checkIdentityBigMatrix(MatrixUtils.createBigIdentityMatrix(3));
        checkIdentityBigMatrix(MatrixUtils.createBigIdentityMatrix(2));
        checkIdentityBigMatrix(MatrixUtils.createBigIdentityMatrix(1));
        try {
            MatrixUtils.createRealIdentityMatrix(0);
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.linear.QRDecompositionImplTest::testDimensions
    public void testDimensions() {
        RealMatrixImpl matrix = new RealMatrixImpl(testData3x3NonSingular, false);
        QRDecomposition qr = new QRDecompositionImpl(matrix);
        assertEquals("3x3 Q size", qr.getQ().getRowDimension(), 3);
        assertEquals("3x3 Q size", qr.getQ().getColumnDimension(), 3);
        assertEquals("3x3 R size", qr.getR().getRowDimension(), 3);
        assertEquals("3x3 R size", qr.getR().getColumnDimension(), 3);

        matrix = new RealMatrixImpl(testData4x3, false);
        qr = new QRDecompositionImpl(matrix);
        assertEquals("4x3 Q size", qr.getQ().getRowDimension(), 4);
        assertEquals("4x3 Q size", qr.getQ().getColumnDimension(), 4);
        assertEquals("4x3 R size", qr.getR().getRowDimension(), 4);
        assertEquals("4x3 R size", qr.getR().getColumnDimension(), 3);

        matrix = new RealMatrixImpl(testData3x4, false);
        qr = new QRDecompositionImpl(matrix);
        assertEquals("3x4 Q size", qr.getQ().getRowDimension(), 3);
        assertEquals("3x4 Q size", qr.getQ().getColumnDimension(), 3);
        assertEquals("3x4 R size", qr.getR().getRowDimension(), 3);
        assertEquals("3x4 R size", qr.getR().getColumnDimension(), 4);
    }

// org.apache.commons.math.linear.QRDecompositionImplTest::testAEqualQR
    public void testAEqualQR() {
        RealMatrix A = new RealMatrixImpl(testData3x3NonSingular, false);
        QRDecomposition qr = new QRDecompositionImpl(A);
        RealMatrix Q = qr.getQ();
        RealMatrix R = qr.getR();
        double norm = Q.multiply(R).subtract(A).getNorm();
        assertEquals("3x3 nonsingular A = QR", 0, norm, normTolerance);

        RealMatrix matrix = new RealMatrixImpl(testData3x3Singular, false);
        qr = new QRDecompositionImpl(matrix);
        norm = qr.getQ().multiply(qr.getR()).subtract(matrix).getNorm();
        assertEquals("3x3 singular A = QR", 0, norm, normTolerance);

        matrix = new RealMatrixImpl(testData3x4, false);
        qr = new QRDecompositionImpl(matrix);
        norm = qr.getQ().multiply(qr.getR()).subtract(matrix).getNorm();
        assertEquals("3x4 A = QR", 0, norm, normTolerance);

        matrix = new RealMatrixImpl(testData4x3, false);
        qr = new QRDecompositionImpl(matrix);
        norm = qr.getQ().multiply(qr.getR()).subtract(matrix).getNorm();
        assertEquals("4x3 A = QR", 0, norm, normTolerance);
    }

// org.apache.commons.math.linear.QRDecompositionImplTest::testQOrthogonal
    public void testQOrthogonal() {
        RealMatrix matrix = new RealMatrixImpl(testData3x3NonSingular, false);
        matrix = new QRDecompositionImpl(matrix).getQ();
        RealMatrix eye = MatrixUtils.createRealIdentityMatrix(3);
        double norm = matrix.transpose().multiply(matrix).subtract(eye)
                .getNorm();
        assertEquals("3x3 nonsingular Q'Q = I", 0, norm, normTolerance);

        matrix = new RealMatrixImpl(testData3x3Singular, false);
        matrix = new QRDecompositionImpl(matrix).getQ();
        eye = MatrixUtils.createRealIdentityMatrix(3);
        norm = matrix.transpose().multiply(matrix).subtract(eye)
                .getNorm();
        assertEquals("3x3 singular Q'Q = I", 0, norm, normTolerance);

        matrix = new RealMatrixImpl(testData3x4, false);
        matrix = new QRDecompositionImpl(matrix).getQ();
        eye = MatrixUtils.createRealIdentityMatrix(3);
        norm = matrix.transpose().multiply(matrix).subtract(eye)
                .getNorm();
        assertEquals("3x4 Q'Q = I", 0, norm, normTolerance);

        matrix = new RealMatrixImpl(testData4x3, false);
        matrix = new QRDecompositionImpl(matrix).getQ();
        eye = MatrixUtils.createRealIdentityMatrix(4);
        norm = matrix.transpose().multiply(matrix).subtract(eye)
                .getNorm();
        assertEquals("4x3 Q'Q = I", 0, norm, normTolerance);
    }

// org.apache.commons.math.linear.QRDecompositionImplTest::testRUpperTriangular
    public void testRUpperTriangular() {
        RealMatrixImpl matrix = new RealMatrixImpl(testData3x3NonSingular, false);
        RealMatrix R = new QRDecompositionImpl(matrix).getR();
        for (int i = 0; i < R.getRowDimension(); i++)
            for (int j = 0; j < i; j++)
                assertEquals("R lower triangle", R.getEntry(i, j), 0,
                        entryTolerance);

        matrix = new RealMatrixImpl(testData3x4, false);
        R = new QRDecompositionImpl(matrix).getR();
        for (int i = 0; i < R.getRowDimension(); i++)
            for (int j = 0; j < i; j++)
                assertEquals("R lower triangle", R.getEntry(i, j), 0,
                        entryTolerance);

        matrix = new RealMatrixImpl(testData4x3, false);
        R = new QRDecompositionImpl(matrix).getR();
        for (int i = 0; i < R.getRowDimension(); i++)
            for (int j = 0; j < i; j++)
                assertEquals("R lower triangle", R.getEntry(i, j), 0,
                        entryTolerance);
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
            ;
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testNorm
    public void testNorm() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPlusMinus
    public void testPlusMinus() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl m2 = new RealMatrixImpl(testDataInv);
        assertClose("m-n = m + -n",m.subtract(m2),
            m2.scalarMultiply(-1d).add(m),entryTolerance);        
        try {
            m.subtract(new RealMatrixImpl(testData2));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMultiply
     public void testMultiply() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
        RealMatrixImpl identity = new RealMatrixImpl(id);
        RealMatrixImpl m2 = new RealMatrixImpl(testData2);
        assertClose("inverse multiply",m.multiply(mInv),
            identity,entryTolerance);
        assertClose("inverse multiply",mInv.multiply(m),
            identity,entryTolerance);
        assertClose("identity multiply",m.multiply(identity),
            m,entryTolerance);
        assertClose("identity multiply",identity.multiply(mInv),
            mInv,entryTolerance);
        assertClose("identity multiply",m2.multiply(identity),
            m2,entryTolerance); 
        try {
            m.multiply(new RealMatrixImpl(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testMultiply2
    public void testMultiply2() { 
       RealMatrix m3 = new RealMatrixImpl(d3);   
       RealMatrix m4 = new RealMatrixImpl(d4);
       RealMatrix m5 = new RealMatrixImpl(d5);
       assertClose("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math.linear.RealMatrixImplTest::testIsSingular
    public void testIsSingular() {
        RealMatrixImpl m = new RealMatrixImpl(singular);
        assertTrue("singular",m.isSingular());
        m = new RealMatrixImpl(bigSingular);
        assertTrue("big singular",m.isSingular());
        m = new RealMatrixImpl(id);
        assertTrue("identity nonsingular",!m.isSingular());
        m = new RealMatrixImpl(testData);
        assertTrue("testData nonsingular",!m.isSingular());
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testInverse
    public void testInverse() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrix mInv = new RealMatrixImpl(testDataInv);
        assertClose("inverse",mInv,m.inverse(),normTolerance);
        assertClose("inverse^2",m,m.inverse().inverse(),10E-12);
        
        
        m = new RealMatrixImpl(testData2);
        try {
            m.inverse();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
        
        
        m = new RealMatrixImpl(singular);
        try {
            m.inverse();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSolve
    public void testSolve() {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrix mInv = new RealMatrixImpl(testDataInv);
        
        assertClose("inverse-operate",mInv.operate(testVector),
            m.solve(testVector),normTolerance);
        try {
            m.solve(testVector2);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }       
        RealMatrix bs = new RealMatrixImpl(bigSingular);
        try {
            bs.solve(bs);
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            ;
        }
        try {
            m.solve(bs);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
        try {
            new RealMatrixImpl(testData2).solve(bs);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        } 
        try {
            (new RealMatrixImpl(testData2)).luDecompose();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            ;
        }  
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testDeterminant
    public void testDeterminant() {       
        RealMatrix m = new RealMatrixImpl(bigSingular);
        assertEquals("singular determinant",0,m.getDeterminant(),0);
        m = new RealMatrixImpl(detData);
        assertEquals("nonsingular test",-3d,m.getDeterminant(),normTolerance);
        
        
        m = new RealMatrixImpl(detData2);
        assertEquals("nonsingular R test 1",-2d,m.getDeterminant(),normTolerance);
        m = new RealMatrixImpl(testData);
        assertEquals("nonsingular  R test 2",-1d,m.getDeterminant(),normTolerance);

        try {
            new RealMatrixImpl(testData2).getDeterminant();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testTrace
    public void testTrace() {
        RealMatrix m = new RealMatrixImpl(id);
        assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new RealMatrixImpl(testData2);
        try {
            m.getTrace();
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new RealMatrixImpl(testData);
        assertClose("scalar add",new RealMatrixImpl(testDataPlus2),
            m.scalarAdd(2d),entryTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testOperate
    public void testOperate() {
        RealMatrix m = new RealMatrixImpl(id);
        double[] x = m.operate(testVector);
        assertClose("identity operate",testVector,x,entryTolerance);
        m = new RealMatrixImpl(bigSingular);
        try {
            m.operate(testVector);
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
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
        assertClose("inverse-transpose",m.inverse().transpose(),
            m.transpose().inverse(),normTolerance);
        m = new RealMatrixImpl(testData2);
        RealMatrix mt = new RealMatrixImpl(testData2T);
        assertClose("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new RealMatrixImpl(testData);
        assertClose("premultiply",m.preMultiply(testVector),preMultTest,normTolerance);
        m = new RealMatrixImpl(bigSingular);
        try {
            m.preMultiply(testVector);
            fail("expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testPremultiply
    public void testPremultiply() {
        RealMatrix m3 = new RealMatrixImpl(d3);   
        RealMatrix m4 = new RealMatrixImpl(d4);
        RealMatrix m5 = new RealMatrixImpl(d5);
        assertClose("m3*m4=m5", m4.preMultiply(m3), m5, entryTolerance);
        
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrixImpl mInv = new RealMatrixImpl(testDataInv);
        RealMatrixImpl identity = new RealMatrixImpl(id);
        assertClose("inverse multiply",m.preMultiply(mInv),
                identity,entryTolerance);
        assertClose("inverse multiply",mInv.preMultiply(m),
                identity,entryTolerance);
        assertClose("identity multiply",m.preMultiply(identity),
                m,entryTolerance);
        assertClose("identity multiply",identity.preMultiply(mInv),
                mInv,entryTolerance);
        try {
            m.preMultiply(new RealMatrixImpl(bigSingular));
            fail("Expecting illegalArgumentException");
        } catch (IllegalArgumentException ex) {
            ;
        }      
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new RealMatrixImpl(testData);
        assertClose("get row",m.getRow(0),testDataRow1,entryTolerance);
        assertClose("get col",m.getColumn(2),testDataCol3,entryTolerance);
        try {
            m.getRow(10);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            ;
        }
        try {
            m.getColumn(-1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            ;
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

// org.apache.commons.math.linear.RealMatrixImplTest::testLUDecomposition
    public void testLUDecomposition() throws Exception {
        RealMatrixImpl m = new RealMatrixImpl(testData);
        RealMatrix lu = m.getLUMatrix();
        assertClose("LU decomposition", lu, (RealMatrix) new RealMatrixImpl(testDataLU), normTolerance);
        verifyDecomposition(m, lu);
        
        lu = m.getLUMatrix();
        assertClose("LU decomposition", lu, (RealMatrix) new RealMatrixImpl(testDataLU), normTolerance);
        verifyDecomposition(m, lu);

        m = new RealMatrixImpl(luData);
        lu = m.getLUMatrix();
        assertClose("LU decomposition", lu, (RealMatrix) new RealMatrixImpl(luDataLUDecomposition), normTolerance);
        verifyDecomposition(m, lu);
        m = new RealMatrixImpl(testDataMinus);
        lu = m.getLUMatrix();
        verifyDecomposition(m, lu);
        m = new RealMatrixImpl(id);
        lu = m.getLUMatrix();
        verifyDecomposition(m, lu);
        try {
            m = new RealMatrixImpl(bigSingular); 
            lu = m.getLUMatrix();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
        }
        try {
            m = new RealMatrixImpl(testData2);  
            lu = m.getLUMatrix();
            fail("Expecting InvalidMatrixException");
        } catch (InvalidMatrixException ex) {
            
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
        
        RealMatrix pInverse = p.inverse(); 
        assertEquals(2, pInverse.getRowDimension());
        assertEquals(2, pInverse.getColumnDimension());
        
        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new RealMatrixImpl(coefficientsData);
        double[] constants = {1, -2, 1};
        double[] solution = coefficients.solve(constants);
        assertEquals(2 * solution[0] + 3 * solution[1] -2 * solution[2], constants[0], 1E-12);
        assertEquals(-1 * solution[0] + 7 * solution[1] + 6 * solution[2], constants[1], 1E-12);
        assertEquals(4 * solution[0] - 3 * solution[1] -5 * solution[2], constants[2], 1E-12);   
        
    }

// org.apache.commons.math.linear.RealMatrixImplTest::testSubMatrix
    public void testSubMatrix() {
        RealMatrix m = new RealMatrixImpl(subTestData);
        RealMatrix mRows23Cols00 = new RealMatrixImpl(subRows23Cols00);
        RealMatrix mRows00Cols33 = new RealMatrixImpl(subRows00Cols33);
        RealMatrix mRows01Cols23 = new RealMatrixImpl(subRows01Cols23);
        RealMatrix mRows02Cols13 = new RealMatrixImpl(subRows02Cols13);
        RealMatrix mRows03Cols12 = new RealMatrixImpl(subRows03Cols12);
        RealMatrix mRows03Cols123 = new RealMatrixImpl(subRows03Cols123);
        RealMatrix mRows20Cols123 = new RealMatrixImpl(subRows20Cols123);
        RealMatrix mRows31Cols31 = new RealMatrixImpl(subRows31Cols31);
        assertEquals("Rows23Cols00", mRows23Cols00, 
                m.getSubMatrix(2 , 3 , 0, 0));
        assertEquals("Rows00Cols33", mRows00Cols33, 
                m.getSubMatrix(0 , 0 , 3, 3));
        assertEquals("Rows01Cols23", mRows01Cols23,
                m.getSubMatrix(0 , 1 , 2, 3));   
        assertEquals("Rows02Cols13", mRows02Cols13,
                m.getSubMatrix(new int[] {0,2}, new int[] {1,3}));  
        assertEquals("Rows03Cols12", mRows03Cols12,
                m.getSubMatrix(new int[] {0,3}, new int[] {1,2}));  
        assertEquals("Rows03Cols123", mRows03Cols123,
                m.getSubMatrix(new int[] {0,3}, new int[] {1,2,3})); 
        assertEquals("Rows20Cols123", mRows20Cols123,
                m.getSubMatrix(new int[] {2,0}, new int[] {1,2,3})); 
        assertEquals("Rows31Cols31", mRows31Cols31,
                m.getSubMatrix(new int[] {3,1}, new int[] {3,1})); 
        assertEquals("Rows31Cols31", mRows31Cols31,
                m.getSubMatrix(new int[] {3,1}, new int[] {3,1})); 
        
        try {
            m.getSubMatrix(1,0,2,4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(-1,1,2,2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1,0,2,2);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(1,0,2,4);
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {}, new int[] {0});
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
        try {
            m.getSubMatrix(new int[] {0}, new int[] {4});
            fail("Expecting MatrixIndexException");
        } catch (MatrixIndexException ex) {
            
        }
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
        
        
        RealMatrixImpl matrix = (RealMatrixImpl) MatrixUtils.createRealMatrix
            (new double[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 0, 1 , 2}});
        matrix.setSubMatrix(new double[][] {{3, 4}, {5, 6}}, 1, 1);
        expected = MatrixUtils.createRealMatrix
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
        RealMatrixImpl m2 = new RealMatrixImpl();
        try {
            m2.setSubMatrix(testData,0,1);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            fail("expecting MatrixIndexException");
        } catch (MatrixIndexException e) {
            
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

// org.apache.commons.math.optimization.NelderMeadTest::testCostExceptions
  public void testCostExceptions() throws ConvergenceException {
      CostFunction wrong =
          new CostFunction() {
            public double cost(double[] x) throws CostException {
                if (x[0] < 0) {
                    throw new CostException("{0}", new Object[] { "oops"});
                } else if (x[0] > 1) {
                    throw new CostException(new RuntimeException("oops"));
                } else {
                    return x[0] * (1 - x[0]);
                }
            }
      };
      try {
          new NelderMead(0.9, 1.9, 0.4, 0.6).minimize(wrong, 10, new ValueChecker(1.0e-3),
                                                      new double[] { -0.5 }, new double[] { 0.5 });
          fail("an exception should have been thrown");
      } catch (CostException ce) {
          
          assertNull(ce.getCause());
      } catch (Exception e) {
          fail("wrong exception caught: " + e.getMessage());
      } 
      try {
          new NelderMead(0.9, 1.9, 0.4, 0.6).minimize(wrong, 10, new ValueChecker(1.0e-3),
                                                      new double[] { 0.5 }, new double[] { 1.5 });
          fail("an exception should have been thrown");
      } catch (CostException ce) {
          
          assertNotNull(ce.getCause());
      } catch (Exception e) {
          fail("wrong exception caught: " + e.getMessage());
      } 
  }

// org.apache.commons.math.optimization.NelderMeadTest::testRosenbrock
  public void testRosenbrock()
    throws CostException, ConvergenceException, NotPositiveDefiniteMatrixException {

    CostFunction rosenbrock =
      new CostFunction() {
        public double cost(double[] x) {
          ++count;
          double a = x[1] - x[0] * x[0];
          double b = 1.0 - x[0];
          return 100 * a * a + b * b;
        }
      };

    count = 0;
    NelderMead nm = new NelderMead();
    try {
      nm.minimize(rosenbrock, 100, new ValueChecker(1.0e-3),
                  new double[][] {
                    { -1.2, 1.0 }, { 3.5, -2.3 }, { 0.4, 1.5 }
                  }, 1, 5384353l);
      fail("an exception should have been thrown");
    } catch (ConvergenceException ce) {
        
    } catch (Exception e) {
        fail("wrong exception caught: " + e.getMessage());
    }

    count = 0;
    PointCostPair optimum =
        nm.minimize(rosenbrock, 100, new ValueChecker(1.0e-3),
                    new double[][] {
                      { -1.2, 1.0 }, { 0.9, 1.2 }, { 3.5, -2.3 }
                    }, 10, 1642738l);

    assertTrue(count > 700);
    assertTrue(count < 800);
    assertEquals(0.0, optimum.getCost(), 5.0e-5);
    assertEquals(1.0, optimum.getPoint()[0], 0.01);
    assertEquals(1.0, optimum.getPoint()[1], 0.01);

    PointCostPair[] minima = nm.getMinima();
    assertEquals(10, minima.length);
    assertNotNull(minima[0]);
    assertNull(minima[minima.length - 1]);
    for (int i = 0; i < minima.length; ++i) {
        if (minima[i] == null) {
            if ((i + 1) < minima.length) {
                assertTrue(minima[i+1] == null);
            }
        } else {
            if (i > 0) {
                assertTrue(minima[i-1].getCost() <= minima[i].getCost());
            }
        }
    }

    RandomGenerator rg = new JDKRandomGenerator();
    rg.setSeed(64453353l);
    RandomVectorGenerator rvg =
        new UncorrelatedRandomVectorGenerator(new double[] { 0.9, 1.1 },
                                              new double[] { 0.2, 0.2 },
                                              new UniformRandomGenerator(rg));
    optimum =
        nm.minimize(rosenbrock, 100, new ValueChecker(1.0e-3), rvg);
    assertEquals(0.0, optimum.getCost(), 2.0e-4);
    optimum =
        nm.minimize(rosenbrock, 100, new ValueChecker(1.0e-3), rvg, 3);
    assertEquals(0.0, optimum.getCost(), 3.0e-5);

  }

// org.apache.commons.math.optimization.NelderMeadTest::testPowell
  public void testPowell()
    throws CostException, ConvergenceException {

    CostFunction powell =
      new CostFunction() {
        public double cost(double[] x) {
          ++count;
          double a = x[0] + 10 * x[1];
          double b = x[2] - x[3];
          double c = x[1] - 2 * x[2];
          double d = x[0] - x[3];
          return a * a + 5 * b * b + c * c * c * c + 10 * d * d * d * d;
        }
      };

    count = 0;
    NelderMead nm = new NelderMead();
    PointCostPair optimum =
      nm.minimize(powell, 200, new ValueChecker(1.0e-3),
                  new double[] {  3.0, -1.0, 0.0, 1.0 },
                  new double[] {  4.0,  0.0, 1.0, 2.0 },
                  1, 1642738l);
    assertTrue(count < 150);
    assertEquals(0.0, optimum.getCost(), 6.0e-4);
    assertEquals(0.0, optimum.getPoint()[0], 0.07);
    assertEquals(0.0, optimum.getPoint()[1], 0.07);
    assertEquals(0.0, optimum.getPoint()[2], 0.07);
    assertEquals(0.0, optimum.getPoint()[3], 0.07);

  }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testSetterInjection
    public void testSetterInjection() throws Exception {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(2, true);
        u.setMeanImpl(new StorelessUnivariateStatistic[] {
                        new sumMean(), new sumMean()
                      });
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        assertEquals(4, u.getMean()[0], 1E-14);
        assertEquals(6, u.getMean()[1], 1E-14);
        u.clear();
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        assertEquals(4, u.getMean()[0], 1E-14);
        assertEquals(6, u.getMean()[1], 1E-14);
        u.clear();
        u.setMeanImpl(new StorelessUnivariateStatistic[] {
                        new Mean(), new Mean()
                      }); 
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        assertEquals(2, u.getMean()[0], 1E-14);
        assertEquals(3, u.getMean()[1], 1E-14);
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testSetterIllegalState
    public void testSetterIllegalState() throws Exception {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(2, true);
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 3, 4 });
        try {
            u.setMeanImpl(new StorelessUnivariateStatistic[] {
                            new sumMean(), new sumMean()
                          });
            fail("Expecting IllegalStateException");
        } catch (IllegalStateException ex) {
            
        }
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testDimension
    public void testDimension() {
        try {
            new MultivariateSummaryStatistics(2, true).addValue(new double[3]);
        } catch (DimensionMismatchException dme) {
            
        } catch (Exception e) {
            fail("wrong exception caught");
        }
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testStats
    public void testStats() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(2, true);
        assertEquals(0, u.getN());
        u.addValue(new double[] { 1, 2 });
        u.addValue(new double[] { 2, 3 });
        u.addValue(new double[] { 2, 3 });
        u.addValue(new double[] { 3, 4 });
        assertEquals( 4, u.getN());
        assertEquals( 8, u.getSum()[0], 1.0e-10);
        assertEquals(12, u.getSum()[1], 1.0e-10);
        assertEquals(18, u.getSumSq()[0], 1.0e-10);
        assertEquals(38, u.getSumSq()[1], 1.0e-10);
        assertEquals( 1, u.getMin()[0], 1.0e-10);
        assertEquals( 2, u.getMin()[1], 1.0e-10);
        assertEquals( 3, u.getMax()[0], 1.0e-10);
        assertEquals( 4, u.getMax()[1], 1.0e-10);
        assertEquals(2.4849066497880003102, u.getSumLog()[0], 1.0e-10);
        assertEquals( 4.276666119016055311, u.getSumLog()[1], 1.0e-10);
        assertEquals( 1.8612097182041991979, u.getGeometricMean()[0], 1.0e-10);
        assertEquals( 2.9129506302439405217, u.getGeometricMean()[1], 1.0e-10);
        assertEquals( 2, u.getMean()[0], 1.0e-10);
        assertEquals( 3, u.getMean()[1], 1.0e-10);
        assertEquals(Math.sqrt(2.0 / 3.0), u.getStandardDeviation()[0], 1.0e-10);
        assertEquals(Math.sqrt(2.0 / 3.0), u.getStandardDeviation()[1], 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(0, 0), 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(0, 1), 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(1, 0), 1.0e-10);
        assertEquals(2.0 / 3.0, u.getCovariance().getEntry(1, 1), 1.0e-10);
        u.clear();
        assertEquals(0, u.getN());    
    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testN0andN1Conditions
    public void testN0andN1Conditions() throws Exception {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(1, true);
        assertTrue(Double.isNaN(u.getMean()[0]));
        assertTrue(Double.isNaN(u.getStandardDeviation()[0]));

        
        u.addValue(new double[] { 1 });
        assertEquals(1.0, u.getMean()[0], 1.0e-10);
        assertEquals(1.0, u.getGeometricMean()[0], 1.0e-10);
        assertEquals(0.0, u.getStandardDeviation()[0], 1.0e-10);

                       
        u.addValue(new double[] { 2 });
        assertTrue(u.getStandardDeviation()[0] > 0);

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testNaNContracts
    public void testNaNContracts() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(1, true);
        assertTrue(Double.isNaN(u.getMean()[0])); 
        assertTrue(Double.isNaN(u.getMin()[0])); 
        assertTrue(Double.isNaN(u.getStandardDeviation()[0])); 
        assertTrue(Double.isNaN(u.getGeometricMean()[0]));

        u.addValue(new double[] { 1.0 });
        assertFalse(Double.isNaN(u.getMean()[0])); 
        assertFalse(Double.isNaN(u.getMin()[0])); 
        assertFalse(Double.isNaN(u.getStandardDeviation()[0])); 
        assertFalse(Double.isNaN(u.getGeometricMean()[0]));

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testSerialization
    public void testSerialization() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(2, true);
        
        TestUtils.checkSerializedEquality(u);
        MultivariateSummaryStatistics s = (MultivariateSummaryStatistics) TestUtils.serializeAndRecover(u);
        assertEquals(u, s);

        
        u.addValue(new double[] { 2d, 1d });
        u.addValue(new double[] { 1d, 1d });
        u.addValue(new double[] { 3d, 1d });
        u.addValue(new double[] { 4d, 1d });
        u.addValue(new double[] { 5d, 1d });

        
        TestUtils.checkSerializedEquality(u);
        s = (MultivariateSummaryStatistics) TestUtils.serializeAndRecover(u);
        assertEquals(u, s);

    }

// org.apache.commons.math.stat.descriptive.MultivariateSummaryStatisticsTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() throws DimensionMismatchException {
        MultivariateSummaryStatistics u = new MultivariateSummaryStatistics(2, true);
        MultivariateSummaryStatistics t = null;
        int emptyHash = u.hashCode();
        assertTrue(u.equals(u));
        assertFalse(u.equals(t));
        assertFalse(u.equals(Double.valueOf(0)));
        t = new MultivariateSummaryStatistics(2, true);
        assertTrue(t.equals(u));
        assertTrue(u.equals(t));
        assertEquals(emptyHash, t.hashCode());

        
        u.addValue(new double[] { 2d, 1d });
        u.addValue(new double[] { 1d, 1d });
        u.addValue(new double[] { 3d, 1d });
        u.addValue(new double[] { 4d, 1d });
        u.addValue(new double[] { 5d, 1d });
        assertFalse(t.equals(u));
        assertFalse(u.equals(t));
        assertTrue(u.hashCode() != t.hashCode());

        
        t.addValue(new double[] { 2d, 1d });
        t.addValue(new double[] { 1d, 1d });
        t.addValue(new double[] { 3d, 1d });
        t.addValue(new double[] { 4d, 1d });
        t.addValue(new double[] { 5d, 1d });
        assertTrue(t.equals(u));
        assertTrue(u.equals(t));
        assertEquals(u.hashCode(), t.hashCode());   

        
        u.clear();
        t.clear();
        assertTrue(t.equals(u));
        assertTrue(u.equals(t));
        assertEquals(emptyHash, t.hashCode());
        assertEquals(emptyHash, u.hashCode());
    }

// org.apache.commons.math.stat.descriptive.moment.VectorialCovarianceTest::testMismatch
    public void testMismatch() {
        try {
            new VectorialCovariance(8, true).increment(new double[5]);
            fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            assertEquals(5, dme.getDimension1());
            assertEquals(8, dme.getDimension2());
        } catch (Exception e) {
            fail("wrong exception type caught: " + e.getClass().getName());
        }
    }

// org.apache.commons.math.stat.descriptive.moment.VectorialCovarianceTest::testSimplistic
    public void testSimplistic() throws DimensionMismatchException {
        VectorialCovariance stat = new VectorialCovariance(2, true);
        stat.increment(new double[] {-1.0,  1.0});
        stat.increment(new double[] { 1.0, -1.0});
        RealMatrix c = stat.getResult();
        assertEquals( 2.0, c.getEntry(0, 0), 1.0e-12);
        assertEquals(-2.0, c.getEntry(1, 0), 1.0e-12);
        assertEquals( 2.0, c.getEntry(1, 1), 1.0e-12);
    }

// org.apache.commons.math.stat.descriptive.moment.VectorialCovarianceTest::testBasicStats
    public void testBasicStats() throws DimensionMismatchException {

        VectorialCovariance stat = new VectorialCovariance(points[0].length, true);
        for (int i = 0; i < points.length; ++i) {
            stat.increment(points[i]);
        }

        assertEquals(points.length, stat.getN());

        RealMatrix c = stat.getResult();
        double[][] refC    = new double[][] {
                { 8.0470, -1.9195, -3.4445},
                {-1.9195,  1.0470,  3.2795},
                {-3.4445,  3.2795, 12.2070}
        };

        for (int i = 0; i < c.getRowDimension(); ++i) {
            for (int j = 0; j <= i; ++j) {
                assertEquals(refC[i][j], c.getEntry(i, j), 1.0e-12);
            }
        }

    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddNullCovarianceData
    public void cannotAddNullCovarianceData() {
        regression.addData(new double[]{}, new double[][]{}, null);
    }

// org.apache.commons.math.stat.regression.GLSMultipleLinearRegressionTest::cannotAddCovarianceDataWithSampleSizeMismatch
    public void cannotAddCovarianceDataWithSampleSizeMismatch() {
        double[] y = new double[]{1.0, 2.0};
        double[][] x = new double[2][];
        x[0] = new double[]{1.0, 0};
        x[1] = new double[]{0, 1.0};
        double[][] omega = new double[1][];
        omega[0] = new double[]{1.0, 0};
        regression.addData(y, x, omega);
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
        regression.addData(y, x, omega);
    }

// org.apache.commons.math.stat.regression.OLSMultipleLinearRegressionTest::testPerfectFit
    public void testPerfectFit() {
        double[] betaHat = regression.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat, 
          new double[]{11.0,0.5,0.666666666666667,0.75,0.8,0.8333333333333333},
                1e-12);
        double[] residuals = regression.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{0d,0d,0d,0d,0d,0d},
                      1e-12);
        double[][] errors = regression.estimateRegressionParametersVariance();
        
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
        double[] y = new double[nobs];
        double[][] x = new double[nobs][nvars + 1];
        loadModelData(design, y, x, nobs, nvars);
        
        
        MultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.addData(y, x, null);
        
        
        double[] betaHat = model.estimateRegressionParameters();
        TestUtils.assertEquals(betaHat, 
          new double[]{-3482258.63459582, 15.0618722713733,
                -0.358191792925910E-01,-2.02022980381683,
                -1.03322686717359,-0.511041056535807E-01,
                 1829.15146461355}, 1E-1); 
        
        
        double[] residuals = model.estimateResiduals();
        TestUtils.assertEquals(residuals, new double[]{
                267.340029759711,-94.0139423988359,46.28716775752924,
                -410.114621930906,309.7145907602313,-249.3112153297231,
                -164.0489563956039,-13.18035686637081,14.30477260005235,
                 455.394094551857,-17.26892711483297,-39.0550425226967,
                -155.5499735953195,-85.6713080421283,341.9315139607727,
                -206.7578251937366},
                      1E-2); 
        
        
        double[][] errors = model.estimateRegressionParametersVariance();
        
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
        double[] y = new double[nobs];
        double[][] x = new double[nobs][nvars + 1];
        loadModelData(design, y, x, nobs, nvars);

        
        MultipleLinearRegression model = new OLSMultipleLinearRegression();
        model.addData(y, x, null);

        
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
    }
