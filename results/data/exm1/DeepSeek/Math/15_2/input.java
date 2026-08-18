// buggy code
    public static double pow(double x, double y) {
        final double lns[] = new double[2];

        if (y == 0.0) {
            return 1.0;
        }

        if (x != x) { // X is NaN
            return x;
        }


        if (x == 0) {
            long bits = Double.doubleToLongBits(x);
            if ((bits & 0x8000000000000000L) != 0) {
                // -zero
                long yi = (long) y;

                if (y < 0 && y == yi && (yi & 1) == 1) {
                    return Double.NEGATIVE_INFINITY;
                }

                if (y > 0 && y == yi && (yi & 1) == 1) {
                    return -0.0;
                }
            }

            if (y < 0) {
                return Double.POSITIVE_INFINITY;
            }
            if (y > 0) {
                return 0.0;
            }

            return Double.NaN;
        }

        if (x == Double.POSITIVE_INFINITY) {
            if (y != y) { // y is NaN
                return y;
            }
            if (y < 0.0) {
                return 0.0;
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }

        if (y == Double.POSITIVE_INFINITY) {
            if (x * x == 1.0) {
                return Double.NaN;
            }

            if (x * x > 1.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                return 0.0;
            }
        }

        if (x == Double.NEGATIVE_INFINITY) {
            if (y != y) { // y is NaN
                return y;
            }

            if (y < 0) {
                long yi = (long) y;
                if (y == yi && (yi & 1) == 1) {
                    return -0.0;
                }

                return 0.0;
            }

            if (y > 0)  {
                long yi = (long) y;
                if (y == yi && (yi & 1) == 1) {
                    return Double.NEGATIVE_INFINITY;
                }

                return Double.POSITIVE_INFINITY;
            }
        }

        if (y == Double.NEGATIVE_INFINITY) {

            if (x * x == 1.0) {
                return Double.NaN;
            }

            if (x * x < 1.0) {
                return Double.POSITIVE_INFINITY;
            } else {
                return 0.0;
            }
        }

        /* Handle special case x<0 */
        if (x < 0) {
            // y is an even integer in this case
            if (y >= TWO_POWER_52 || y <= -TWO_POWER_52) {
                return pow(-x, y);
            }

            if (y == (long) y) {
                // If y is an integer
                return ((long)y & 1) == 0 ? pow(-x, y) : -pow(-x, y);
            } else {
                return Double.NaN;
            }
        }

        /* Split y into ya and yb such that y = ya+yb */
        double ya;
        double yb;
        if (y < 8e298 && y > -8e298) {
            double tmp1 = y * HEX_40000000;
            ya = y + tmp1 - tmp1;
            yb = y - ya;
        } else {
            double tmp1 = y * 9.31322574615478515625E-10;
            double tmp2 = tmp1 * 9.31322574615478515625E-10;
            ya = (tmp1 + tmp2 - tmp1) * HEX_40000000 * HEX_40000000;
            yb = y - ya;
        }

        /* Compute ln(x) */
        final double lores = log(x, lns);
        if (Double.isInfinite(lores)){ // don't allow this to be converted to NaN
            return lores;
        }

        double lna = lns[0];
        double lnb = lns[1];

        /* resplit lns */
        double tmp1 = lna * HEX_40000000;
        double tmp2 = lna + tmp1 - tmp1;
        lnb += lna - tmp2;
        lna = tmp2;

        // y*ln(x) = (aa+ab)
        final double aa = lna * ya;
        final double ab = lna * yb + lnb * ya + lnb * yb;

        lna = aa+ab;
        lnb = -(lna - aa - ab);

        double z = 1.0 / 120.0;
        z = z * lnb + (1.0 / 24.0);
        z = z * lnb + (1.0 / 6.0);
        z = z * lnb + 0.5;
        z = z * lnb + 1.0;
        z = z * lnb;

        final double result = exp(lna, z, null);
        //result = result + result * z;
        return result;
    }

// relevant test
// org.apache.commons.math3.distribution.PoissonDistributionTest::testNormalApproximateProbability
    public void testNormalApproximateProbability() {
        PoissonDistribution dist = new PoissonDistribution(100);
        double result = dist.normalApproximateProbability(110)
                - dist.normalApproximateProbability(89);
        Assert.assertEquals(0.706281887248, result, 1E-10);

        dist = new PoissonDistribution(10000);
        result = dist.normalApproximateProbability(10200)
        - dist.normalApproximateProbability(9899);
        Assert.assertEquals(0.820070051552, result, 1E-10);
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testDegenerateInverseCumulativeProbability
    public void testDegenerateInverseCumulativeProbability() {
        PoissonDistribution dist = new PoissonDistribution(DEFAULT_TEST_POISSON_PARAMETER);
        Assert.assertEquals(Integer.MAX_VALUE, dist.inverseCumulativeProbability(1.0d));
        Assert.assertEquals(0, dist.inverseCumulativeProbability(0d));
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testNegativeMean
    public void testNegativeMean() {
        new PoissonDistribution(-1);
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testMean
    public void testMean() {
        PoissonDistribution dist = new PoissonDistribution(10.0);
        Assert.assertEquals(10.0, dist.getMean(), 0.0);
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testLargeMeanCumulativeProbability
    public void testLargeMeanCumulativeProbability() {
        double mean = 1.0;
        while (mean <= 10000000.0) {
            PoissonDistribution dist = new PoissonDistribution(mean);

            double x = mean * 2.0;
            double dx = x / 10.0;
            double p = Double.NaN;
            double sigma = FastMath.sqrt(mean);
            while (x >= 0) {
                try {
                    p = dist.cumulativeProbability((int) x);
                    Assert.assertFalse("NaN cumulative probability returned for mean = " +
                            mean + " x = " + x,Double.isNaN(p));
                    if (x > mean - 2 * sigma) {
                        Assert.assertTrue("Zero cum probaility returned for mean = " +
                                mean + " x = " + x, p > 0);
                    }
                } catch (Exception ex) {
                    Assert.fail("mean of " + mean + " and x of " + x + " caused " + ex.getMessage());
                }
                x -= dx;
            }

            mean *= 10.0;
        }
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testCumulativeProbabilitySpecial
    public void testCumulativeProbabilitySpecial() {
        PoissonDistribution dist;
        dist = new PoissonDistribution(9120);
        checkProbability(dist, 9075);
        checkProbability(dist, 9102);
        dist = new PoissonDistribution(5058);
        checkProbability(dist, 5044);
        dist = new PoissonDistribution(6986);
        checkProbability(dist, 6950);
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testLargeMeanInverseCumulativeProbability
    public void testLargeMeanInverseCumulativeProbability() {
        double mean = 1.0;
        while (mean <= 100000.0) { 
            PoissonDistribution dist = new PoissonDistribution(mean);
            double p = 0.1;
            double dp = p;
            while (p < .99) {
                try {
                    int ret = dist.inverseCumulativeProbability(p);
                    
                    Assert.assertTrue(p <= dist.cumulativeProbability(ret));
                    Assert.assertTrue(p > dist.cumulativeProbability(ret - 1));
                } catch (Exception ex) {
                    Assert.fail("mean of " + mean + " and p of " + p + " caused " + ex.getMessage());
                }
                p += dp;
            }
            mean *= 10.0;
        }
    }

// org.apache.commons.math3.distribution.PoissonDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        PoissonDistribution dist;

        dist = new PoissonDistribution(1);
        Assert.assertEquals(dist.getNumericalMean(), 1, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1, tol);

        dist = new PoissonDistribution(11.23);
        Assert.assertEquals(dist.getNumericalMean(), 11.23, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 11.23, tol);
    }

// org.apache.commons.math3.distribution.TDistributionTest::testCumulativeProbabilityAgainstStackOverflow
    public void testCumulativeProbabilityAgainstStackOverflow() {
        TDistribution td = new TDistribution(5.);
        td.cumulativeProbability(.1);
        td.cumulativeProbability(.01);
    }

// org.apache.commons.math3.distribution.TDistributionTest::testSmallDf
    public void testSmallDf() {
        setDistribution(new TDistribution(1d));
        
        setCumulativeTestPoints(new double[] {-318.308838986, -31.8205159538, -12.7062047362,
                -6.31375151468, -3.07768353718, 318.308838986, 31.8205159538, 12.7062047362,
                 6.31375151468, 3.07768353718});
        setDensityTestValues(new double[] {3.14158231817e-06, 0.000314055924703, 0.00195946145194,
                0.00778959736375, 0.0303958893917, 3.14158231817e-06, 0.000314055924703,
                0.00195946145194, 0.00778959736375, 0.0303958893917});
        setInverseCumulativeTestValues(getCumulativeTestPoints());
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
        verifyDensities();
    }

// org.apache.commons.math3.distribution.TDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.TDistributionTest::testDfAccessors
    public void testDfAccessors() {
        TDistribution dist = (TDistribution) getDistribution();
        Assert.assertEquals(5d, dist.getDegreesOfFreedom(), Double.MIN_VALUE);
    }

// org.apache.commons.math3.distribution.TDistributionTest::testPreconditions
    public void testPreconditions() {
        new TDistribution(0);
    }

// org.apache.commons.math3.distribution.TDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        TDistribution dist;

        dist = new TDistribution(1);
        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));

        dist = new TDistribution(1.5);
        Assert.assertEquals(dist.getNumericalMean(), 0, tol);
        Assert.assertTrue(Double.isInfinite(dist.getNumericalVariance()));

        dist = new TDistribution(5);
        Assert.assertEquals(dist.getNumericalMean(), 0, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 5d / (5d - 2d), tol);
    }

// org.apache.commons.math3.distribution.TDistributionTest::nistData
    public void nistData(){
        double[] prob = new double[]{ 0.10,0.05,0.025,0.01,0.005,0.001};
        double[] args2 = new double[]{1.886,2.920,4.303,6.965,9.925,22.327};
        double[] args10 = new double[]{1.372,1.812,2.228,2.764,3.169,4.143};
        double[] args30 = new double[]{1.310,1.697,2.042,2.457,2.750,3.385};
        double[] args100= new double[]{1.290,1.660,1.984,2.364,2.626,3.174};
        TestUtils.assertEquals(prob, makeNistResults(args2, 2), 1.0e-4);
        TestUtils.assertEquals(prob, makeNistResults(args10, 10), 1.0e-4);
        TestUtils.assertEquals(prob, makeNistResults(args30, 30), 1.0e-4);
        TestUtils.assertEquals(prob, makeNistResults(args100, 100), 1.0e-4);
        return;
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testGetLowerBound
    public void testGetLowerBound() {
        TriangularDistribution distribution = makeDistribution();
        Assert.assertEquals(-3.0, distribution.getSupportLowerBound(), 0);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testGetUpperBound
    public void testGetUpperBound() {
        TriangularDistribution distribution = makeDistribution();
        Assert.assertEquals(12.0, distribution.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testPreconditions1
    public void testPreconditions1() {
        new TriangularDistribution(0, 0, 0);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testPreconditions2
    public void testPreconditions2() {
        new TriangularDistribution(1, 1, 0);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testPreconditions3
    public void testPreconditions3() {
        new TriangularDistribution(0, 2, 1);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testPreconditions4
    public void testPreconditions4() {
        new TriangularDistribution(2, 1, 3);
    }

// org.apache.commons.math3.distribution.TriangularDistributionTest::testMeanVariance
    public void testMeanVariance() {
        TriangularDistribution dist;

        dist = new TriangularDistribution(0, 0.5, 1.0);
        Assert.assertEquals(dist.getNumericalMean(), 0.5, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 1 / 24.0, 0);

        dist = new TriangularDistribution(0, 1, 1);
        Assert.assertEquals(dist.getNumericalMean(), 2 / 3.0, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 1 / 18.0, 0);

        dist = new TriangularDistribution(-3, 2, 12);
        Assert.assertEquals(dist.getNumericalMean(), 3 + (2 / 3.0), 0);
        Assert.assertEquals(dist.getNumericalVariance(), 175 / 18.0, 0);
    }

// org.apache.commons.math3.distribution.UniformIntegerDistributionTest::testMoments
    public void testMoments() {
        UniformIntegerDistribution dist;

        dist = new UniformIntegerDistribution(0, 5);
        Assert.assertEquals(dist.getNumericalMean(), 2.5, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 35 / 12.0, 0);

        dist = new UniformIntegerDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 0.5, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 3 / 12.0, 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testGetLowerBound
    public void testGetLowerBound() {
        UniformRealDistribution distribution = makeDistribution();
        Assert.assertEquals(-0.5, distribution.getSupportLowerBound(), 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testGetUpperBound
    public void testGetUpperBound() {
        UniformRealDistribution distribution = makeDistribution();
        Assert.assertEquals(1.25, distribution.getSupportUpperBound(), 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testPreconditions1
    public void testPreconditions1() {
        new UniformRealDistribution(0, 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testPreconditions2
    public void testPreconditions2() {
        new UniformRealDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testMeanVariance
    public void testMeanVariance() {
        UniformRealDistribution dist;

        dist = new UniformRealDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 0.5, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 1/12.0, 0);

        dist = new UniformRealDistribution(-1.5, 0.6);
        Assert.assertEquals(dist.getNumericalMean(), -0.45, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 0.3675, 0);

        dist = new UniformRealDistribution(-0.5, 1.25);
        Assert.assertEquals(dist.getNumericalMean(), 0.375, 0);
        Assert.assertEquals(dist.getNumericalVariance(), 0.2552083333333333, 0);
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0.0, 1.0});
        setInverseCumulativeTestValues(
                new double[] {0.0, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testAlpha
    public void testAlpha() {
        WeibullDistribution dist = new WeibullDistribution(1, 2);
        Assert.assertEquals(1, dist.getShape(), 0);
        try {
            dist = new WeibullDistribution(0, 2);
            Assert.fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testBeta
    public void testBeta() {
        WeibullDistribution dist = new WeibullDistribution(1, 2);
        Assert.assertEquals(2, dist.getScale(), 0);
        try {
            dist = new WeibullDistribution(1, 0);
            Assert.fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        WeibullDistribution dist;

        dist = new WeibullDistribution(2.5, 3.5);
        
        Assert.assertEquals(dist.getNumericalMean(), 3.5 * FastMath.exp(Gamma.logGamma(1 + (1 / 2.5))), tol);
        Assert.assertEquals(dist.getNumericalVariance(), (3.5 * 3.5) *
                FastMath.exp(Gamma.logGamma(1 + (2 / 2.5))) -
                (dist.getNumericalMean() * dist.getNumericalMean()), tol);

        dist = new WeibullDistribution(10.4, 2.222);
        Assert.assertEquals(dist.getNumericalMean(), 2.222 * FastMath.exp(Gamma.logGamma(1 + (1 / 10.4))), tol);
        Assert.assertEquals(dist.getNumericalVariance(), (2.222 * 2.222) *
                FastMath.exp(Gamma.logGamma(1 + (2 / 10.4))) -
                (dist.getNumericalMean() * dist.getNumericalMean()), tol);
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testSampling
    public void testSampling() {}

// org.apache.commons.math3.distribution.ZipfDistributionTest::testPreconditions1
    public void testPreconditions1() {
        new ZipfDistribution(0, 1);
    }

// org.apache.commons.math3.distribution.ZipfDistributionTest::testPreconditions2
    public void testPreconditions2() {
        new ZipfDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.ZipfDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        ZipfDistribution dist;

        dist = new ZipfDistribution(2, 0.5);
        Assert.assertEquals(dist.getNumericalMean(), FastMath.sqrt(2), tol);
        Assert.assertEquals(dist.getNumericalVariance(), 0.24264068711928521, tol);
    }

// org.apache.commons.math3.filter.KalmanFilterTest::testTransitionMeasurementMatrixMismatch
    public void testTransitionMeasurementMatrixMismatch() {
        
        
        
        
        RealMatrix A = new Array2DRowRealMatrix(new double[] { 1d });
        
        RealMatrix B = null;
        
        RealMatrix H = new Array2DRowRealMatrix(new double[] { 1d, 1d });
        
        RealMatrix Q = new Array2DRowRealMatrix(new double[] { 0 });
        
        RealMatrix R = new Array2DRowRealMatrix(new double[] { 0 });

        ProcessModel pm
            = new DefaultProcessModel(A, B, Q,
                                      new ArrayRealVector(new double[] { 0 }), null);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        new KalmanFilter(pm, mm);
        Assert.fail("transition and measurement matrix should not be compatible");
    }

// org.apache.commons.math3.filter.KalmanFilterTest::testTransitionControlMatrixMismatch
    public void testTransitionControlMatrixMismatch() {
        
        
        
        
        RealMatrix A = new Array2DRowRealMatrix(new double[] { 1d });
        
        RealMatrix B = new Array2DRowRealMatrix(new double[] { 1d, 1d });
        
        RealMatrix H = new Array2DRowRealMatrix(new double[] { 1d });
        
        RealMatrix Q = new Array2DRowRealMatrix(new double[] { 0 });
        
        RealMatrix R = new Array2DRowRealMatrix(new double[] { 0 });

        ProcessModel pm
            = new DefaultProcessModel(A, B, Q,
                                      new ArrayRealVector(new double[] { 0 }), null);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        new KalmanFilter(pm, mm);
        Assert.fail("transition and control matrix should not be compatible");
    }

// org.apache.commons.math3.filter.KalmanFilterTest::testConstant
    public void testConstant() {
        
        
        double constantValue = 10d;
        double measurementNoise = 0.1d;
        double processNoise = 1e-5d;

        
        RealMatrix A = new Array2DRowRealMatrix(new double[] { 1d });
        
        RealMatrix B = null;
        
        RealMatrix H = new Array2DRowRealMatrix(new double[] { 1d });
        
        RealVector x = new ArrayRealVector(new double[] { constantValue });
        
        RealMatrix Q = new Array2DRowRealMatrix(new double[] { processNoise });
        
        RealMatrix R = new Array2DRowRealMatrix(new double[] { measurementNoise });

        ProcessModel pm
            = new DefaultProcessModel(A, B, Q,
                                      new ArrayRealVector(new double[] { constantValue }), null);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        KalmanFilter filter = new KalmanFilter(pm, mm);

        Assert.assertEquals(1, filter.getMeasurementDimension());
        Assert.assertEquals(1, filter.getStateDimension());

        assertMatrixEquals(Q.getData(), filter.getErrorCovariance());

        
        double[] expectedInitialState = new double[] { constantValue };
        assertVectorEquals(expectedInitialState, filter.getStateEstimation());

        RealVector pNoise = new ArrayRealVector(1);
        RealVector mNoise = new ArrayRealVector(1);

        RandomGenerator rand = new JDKRandomGenerator();
        
        for (int i = 0; i < 60; i++) {
            filter.predict();

            
            pNoise.setEntry(0, processNoise * rand.nextGaussian());

            
            x = A.operate(x).add(pNoise);

            
            mNoise.setEntry(0, measurementNoise * rand.nextGaussian());

            
            RealVector z = H.operate(x).add(mNoise);

            filter.correct(z);

            
            double diff = Math.abs(constantValue - filter.getStateEstimation()[0]);
            
            Assert.assertTrue(Precision.compareTo(diff, measurementNoise, 1e-6) < 0);
        }

        
        Assert.assertTrue(Precision.compareTo(filter.getErrorCovariance()[0][0],
                                              0.02d, 1e-6) < 0);
    }

// org.apache.commons.math3.filter.KalmanFilterTest::testConstantAcceleration
    public void testConstantAcceleration() {
        

        
        double dt = 0.1d;
        
        double measurementNoise = 10d;
        
        double accelNoise = 0.2d;

        
        
        RealMatrix A = new Array2DRowRealMatrix(new double[][] { { 1, dt }, { 0, 1 } });

        
        
        RealMatrix B = new Array2DRowRealMatrix(
                new double[][] { { Math.pow(dt, 2d) / 2d }, { dt } });

        
        RealMatrix H = new Array2DRowRealMatrix(new double[][] { { 1d, 0d } });

        
        RealVector x = new ArrayRealVector(new double[] { 0, 0 });

        RealMatrix tmp = new Array2DRowRealMatrix(
                new double[][] { { Math.pow(dt, 4d) / 4d, Math.pow(dt, 3d) / 2d },
                                 { Math.pow(dt, 3d) / 2d, Math.pow(dt, 2d) } });

        
        
        RealMatrix Q = tmp.scalarMultiply(Math.pow(accelNoise, 2));

        
        
        RealMatrix P0 = new Array2DRowRealMatrix(new double[][] { { 1, 1 }, { 1, 1 } });

        
        RealMatrix R = new Array2DRowRealMatrix(
                new double[] { Math.pow(measurementNoise, 2) });

        
        RealVector u = new ArrayRealVector(new double[] { 0.1d });

        ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);
        KalmanFilter filter = new KalmanFilter(pm, mm);

        Assert.assertEquals(1, filter.getMeasurementDimension());
        Assert.assertEquals(2, filter.getStateDimension());

        assertMatrixEquals(P0.getData(), filter.getErrorCovariance());

        
        double[] expectedInitialState = new double[] { 0.0, 0.0 };
        assertVectorEquals(expectedInitialState, filter.getStateEstimation());

        RandomGenerator rand = new JDKRandomGenerator();

        RealVector tmpPNoise = new ArrayRealVector(
                new double[] { Math.pow(dt, 2d) / 2d, dt });

        RealVector mNoise = new ArrayRealVector(1);

        
        for (int i = 0; i < 60; i++) {
            filter.predict(u);

            
            RealVector pNoise = tmpPNoise.mapMultiply(accelNoise * rand.nextGaussian());

            
            x = A.operate(x).add(B.operate(u)).add(pNoise);

            
            mNoise.setEntry(0, measurementNoise * rand.nextGaussian());

            
            RealVector z = H.operate(x).add(mNoise);

            filter.correct(z);

            
            double diff = Math.abs(x.getEntry(0) - filter.getStateEstimation()[0]);
            Assert.assertTrue(Precision.compareTo(diff, measurementNoise, 1e-6) < 0);
        }

        
        Assert.assertTrue(Precision.compareTo(filter.getErrorCovariance()[1][1],
                                              0.1d, 1e-6) < 0);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testConstructor
    public void testConstructor() {
        assertFraction(0, 1, new BigFraction(0, 1));
        assertFraction(0, 1, new BigFraction(0l, 2l));
        assertFraction(0, 1, new BigFraction(0, -1));
        assertFraction(1, 2, new BigFraction(1, 2));
        assertFraction(1, 2, new BigFraction(2, 4));
        assertFraction(-1, 2, new BigFraction(-1, 2));
        assertFraction(-1, 2, new BigFraction(1, -2));
        assertFraction(-1, 2, new BigFraction(-2, 4));
        assertFraction(-1, 2, new BigFraction(2, -4));
        assertFraction(11, 1, new BigFraction(11));
        assertFraction(11, 1, new BigFraction(11l));
        assertFraction(11, 1, new BigFraction(new BigInteger("11")));

        assertFraction(0, 1, new BigFraction(0.00000000000001, 1.0e-5, 100));
        assertFraction(2, 5, new BigFraction(0.40000000000001, 1.0e-5, 100));
        assertFraction(15, 1, new BigFraction(15.0000000000001, 1.0e-5, 100));

        Assert.assertEquals(0.00000000000001, new BigFraction(0.00000000000001).doubleValue(), 0.0);
        Assert.assertEquals(0.40000000000001, new BigFraction(0.40000000000001).doubleValue(), 0.0);
        Assert.assertEquals(15.0000000000001, new BigFraction(15.0000000000001).doubleValue(), 0.0);
        assertFraction(3602879701896487l, 9007199254740992l, new BigFraction(0.40000000000001));
        assertFraction(1055531162664967l, 70368744177664l, new BigFraction(15.0000000000001));
        try {
            new BigFraction(null, BigInteger.ONE);
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException npe) {
            
        }
        try {
            new BigFraction(BigInteger.ONE, null);
            Assert.fail("Expecting NullArgumentException");
        } catch (NullArgumentException npe) {
            
        }
        try {
            new BigFraction(BigInteger.ONE, BigInteger.ZERO);
            Assert.fail("Expecting ZeroException");
        } catch (ZeroException npe) {
            
        }
        try {
            new BigFraction(2.0 * Integer.MAX_VALUE, 1.0e-5, 100000);
            Assert.fail("Expecting FractionConversionException");
        } catch (FractionConversionException fce) {
            
        }
    }

// org.apache.commons.math3.fraction.BigFractionTest::testGoldenRatio
    public void testGoldenRatio() {
        
        new BigFraction((1 + FastMath.sqrt(5)) / 2, 1.0e-12, 25);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testDoubleConstructor
    public void testDoubleConstructor() throws ConvergenceException {
        assertFraction(1, 2, new BigFraction((double) 1 / (double) 2, 1.0e-5, 100));
        assertFraction(1, 3, new BigFraction((double) 1 / (double) 3, 1.0e-5, 100));
        assertFraction(2, 3, new BigFraction((double) 2 / (double) 3, 1.0e-5, 100));
        assertFraction(1, 4, new BigFraction((double) 1 / (double) 4, 1.0e-5, 100));
        assertFraction(3, 4, new BigFraction((double) 3 / (double) 4, 1.0e-5, 100));
        assertFraction(1, 5, new BigFraction((double) 1 / (double) 5, 1.0e-5, 100));
        assertFraction(2, 5, new BigFraction((double) 2 / (double) 5, 1.0e-5, 100));
        assertFraction(3, 5, new BigFraction((double) 3 / (double) 5, 1.0e-5, 100));
        assertFraction(4, 5, new BigFraction((double) 4 / (double) 5, 1.0e-5, 100));
        assertFraction(1, 6, new BigFraction((double) 1 / (double) 6, 1.0e-5, 100));
        assertFraction(5, 6, new BigFraction((double) 5 / (double) 6, 1.0e-5, 100));
        assertFraction(1, 7, new BigFraction((double) 1 / (double) 7, 1.0e-5, 100));
        assertFraction(2, 7, new BigFraction((double) 2 / (double) 7, 1.0e-5, 100));
        assertFraction(3, 7, new BigFraction((double) 3 / (double) 7, 1.0e-5, 100));
        assertFraction(4, 7, new BigFraction((double) 4 / (double) 7, 1.0e-5, 100));
        assertFraction(5, 7, new BigFraction((double) 5 / (double) 7, 1.0e-5, 100));
        assertFraction(6, 7, new BigFraction((double) 6 / (double) 7, 1.0e-5, 100));
        assertFraction(1, 8, new BigFraction((double) 1 / (double) 8, 1.0e-5, 100));
        assertFraction(3, 8, new BigFraction((double) 3 / (double) 8, 1.0e-5, 100));
        assertFraction(5, 8, new BigFraction((double) 5 / (double) 8, 1.0e-5, 100));
        assertFraction(7, 8, new BigFraction((double) 7 / (double) 8, 1.0e-5, 100));
        assertFraction(1, 9, new BigFraction((double) 1 / (double) 9, 1.0e-5, 100));
        assertFraction(2, 9, new BigFraction((double) 2 / (double) 9, 1.0e-5, 100));
        assertFraction(4, 9, new BigFraction((double) 4 / (double) 9, 1.0e-5, 100));
        assertFraction(5, 9, new BigFraction((double) 5 / (double) 9, 1.0e-5, 100));
        assertFraction(7, 9, new BigFraction((double) 7 / (double) 9, 1.0e-5, 100));
        assertFraction(8, 9, new BigFraction((double) 8 / (double) 9, 1.0e-5, 100));
        assertFraction(1, 10, new BigFraction((double) 1 / (double) 10, 1.0e-5, 100));
        assertFraction(3, 10, new BigFraction((double) 3 / (double) 10, 1.0e-5, 100));
        assertFraction(7, 10, new BigFraction((double) 7 / (double) 10, 1.0e-5, 100));
        assertFraction(9, 10, new BigFraction((double) 9 / (double) 10, 1.0e-5, 100));
        assertFraction(1, 11, new BigFraction((double) 1 / (double) 11, 1.0e-5, 100));
        assertFraction(2, 11, new BigFraction((double) 2 / (double) 11, 1.0e-5, 100));
        assertFraction(3, 11, new BigFraction((double) 3 / (double) 11, 1.0e-5, 100));
        assertFraction(4, 11, new BigFraction((double) 4 / (double) 11, 1.0e-5, 100));
        assertFraction(5, 11, new BigFraction((double) 5 / (double) 11, 1.0e-5, 100));
        assertFraction(6, 11, new BigFraction((double) 6 / (double) 11, 1.0e-5, 100));
        assertFraction(7, 11, new BigFraction((double) 7 / (double) 11, 1.0e-5, 100));
        assertFraction(8, 11, new BigFraction((double) 8 / (double) 11, 1.0e-5, 100));
        assertFraction(9, 11, new BigFraction((double) 9 / (double) 11, 1.0e-5, 100));
        assertFraction(10, 11, new BigFraction((double) 10 / (double) 11, 1.0e-5, 100));
    }

// org.apache.commons.math3.fraction.BigFractionTest::testDigitLimitConstructor
    public void testDigitLimitConstructor() throws ConvergenceException {
        assertFraction(2, 5, new BigFraction(0.4, 9));
        assertFraction(2, 5, new BigFraction(0.4, 99));
        assertFraction(2, 5, new BigFraction(0.4, 999));

        assertFraction(3, 5, new BigFraction(0.6152, 9));
        assertFraction(8, 13, new BigFraction(0.6152, 99));
        assertFraction(510, 829, new BigFraction(0.6152, 999));
        assertFraction(769, 1250, new BigFraction(0.6152, 9999));
    }

// org.apache.commons.math3.fraction.BigFractionTest::testEpsilonLimitConstructor
    public void testEpsilonLimitConstructor() throws ConvergenceException {
        assertFraction(2, 5, new BigFraction(0.4, 1.0e-5, 100));

        assertFraction(3, 5, new BigFraction(0.6152, 0.02, 100));
        assertFraction(8, 13, new BigFraction(0.6152, 1.0e-3, 100));
        assertFraction(251, 408, new BigFraction(0.6152, 1.0e-4, 100));
        assertFraction(251, 408, new BigFraction(0.6152, 1.0e-5, 100));
        assertFraction(510, 829, new BigFraction(0.6152, 1.0e-6, 100));
        assertFraction(769, 1250, new BigFraction(0.6152, 1.0e-7, 100));
    }

// org.apache.commons.math3.fraction.BigFractionTest::testCompareTo
    public void testCompareTo() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);
        BigFraction third = new BigFraction(1, 2);

        Assert.assertEquals(0, first.compareTo(first));
        Assert.assertEquals(0, first.compareTo(third));
        Assert.assertEquals(1, first.compareTo(second));
        Assert.assertEquals(-1, second.compareTo(first));

        
        
        
        BigFraction pi1 = new BigFraction(1068966896, 340262731);
        BigFraction pi2 = new BigFraction( 411557987, 131002976);
        Assert.assertEquals(-1, pi1.compareTo(pi2));
        Assert.assertEquals( 1, pi2.compareTo(pi1));
        Assert.assertEquals(0.0, pi1.doubleValue() - pi2.doubleValue(), 1.0e-20);

    }

// org.apache.commons.math3.fraction.BigFractionTest::testDoubleValue
    public void testDoubleValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);

        Assert.assertEquals(0.5, first.doubleValue(), 0.0);
        Assert.assertEquals(1.0 / 3.0, second.doubleValue(), 0.0);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testDoubleValueForLargeNumeratorAndDenominator
    public void testDoubleValueForLargeNumeratorAndDenominator() {
        final BigInteger pow400 = BigInteger.TEN.pow(400);
        final BigInteger pow401 = BigInteger.TEN.pow(401);
        final BigInteger two = new BigInteger("2");
        final BigFraction large = new BigFraction(pow401.add(BigInteger.ONE),
                                                  pow400.multiply(two));

        Assert.assertEquals(5, large.doubleValue(), 1e-15);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testFloatValueForLargeNumeratorAndDenominator
    public void testFloatValueForLargeNumeratorAndDenominator() {
        final BigInteger pow400 = BigInteger.TEN.pow(400);
        final BigInteger pow401 = BigInteger.TEN.pow(401);
        final BigInteger two = new BigInteger("2");
        final BigFraction large = new BigFraction(pow401.add(BigInteger.ONE),
                                                  pow400.multiply(two));

        Assert.assertEquals(5, large.floatValue(), 1e-15);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testFloatValue
    public void testFloatValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(1, 3);

        Assert.assertEquals(0.5f, first.floatValue(), 0.0f);
        Assert.assertEquals((float) (1.0 / 3.0), second.floatValue(), 0.0f);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testIntValue
    public void testIntValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(3, 2);

        Assert.assertEquals(0, first.intValue());
        Assert.assertEquals(1, second.intValue());
    }

// org.apache.commons.math3.fraction.BigFractionTest::testLongValue
    public void testLongValue() {
        BigFraction first = new BigFraction(1, 2);
        BigFraction second = new BigFraction(3, 2);

        Assert.assertEquals(0L, first.longValue());
        Assert.assertEquals(1L, second.longValue());
    }

// org.apache.commons.math3.fraction.BigFractionTest::testConstructorDouble
    public void testConstructorDouble() {
        assertFraction(1, 2, new BigFraction(0.5));
        assertFraction(6004799503160661l, 18014398509481984l, new BigFraction(1.0 / 3.0));
        assertFraction(6124895493223875l, 36028797018963968l, new BigFraction(17.0 / 100.0));
        assertFraction(1784551352345559l, 562949953421312l, new BigFraction(317.0 / 100.0));
        assertFraction(-1, 2, new BigFraction(-0.5));
        assertFraction(-6004799503160661l, 18014398509481984l, new BigFraction(-1.0 / 3.0));
        assertFraction(-6124895493223875l, 36028797018963968l, new BigFraction(17.0 / -100.0));
        assertFraction(-1784551352345559l, 562949953421312l, new BigFraction(-317.0 / 100.0));
        for (double v : new double[] { Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            try {
                new BigFraction(v);
                Assert.fail("Expecting IllegalArgumentException");
            } catch (IllegalArgumentException iae) {
                
            }
        }
        Assert.assertEquals(1l, new BigFraction(Double.MAX_VALUE).getDenominatorAsLong());
        Assert.assertEquals(1l, new BigFraction(Double.longBitsToDouble(0x0010000000000000L)).getNumeratorAsLong());
        Assert.assertEquals(1l, new BigFraction(Double.MIN_VALUE).getNumeratorAsLong());
    }

// org.apache.commons.math3.fraction.BigFractionTest::testAbs
    public void testAbs() {
        BigFraction a = new BigFraction(10, 21);
        BigFraction b = new BigFraction(-10, 21);
        BigFraction c = new BigFraction(10, -21);

        assertFraction(10, 21, a.abs());
        assertFraction(10, 21, b.abs());
        assertFraction(10, 21, c.abs());
    }

// org.apache.commons.math3.fraction.BigFractionTest::testReciprocal
    public void testReciprocal() {
        BigFraction f = null;

        f = new BigFraction(50, 75);
        f = f.reciprocal();
        Assert.assertEquals(3, f.getNumeratorAsInt());
        Assert.assertEquals(2, f.getDenominatorAsInt());

        f = new BigFraction(4, 3);
        f = f.reciprocal();
        Assert.assertEquals(3, f.getNumeratorAsInt());
        Assert.assertEquals(4, f.getDenominatorAsInt());

        f = new BigFraction(-15, 47);
        f = f.reciprocal();
        Assert.assertEquals(-47, f.getNumeratorAsInt());
        Assert.assertEquals(15, f.getDenominatorAsInt());

        f = new BigFraction(0, 3);
        try {
            f = f.reciprocal();
            Assert.fail("expecting ZeroException");
        } catch (ZeroException ex) {
        }

        
        f = new BigFraction(Integer.MAX_VALUE, 1);
        f = f.reciprocal();
        Assert.assertEquals(1, f.getNumeratorAsInt());
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
    }

// org.apache.commons.math3.fraction.BigFractionTest::testNegate
    public void testNegate() {
        BigFraction f = null;

        f = new BigFraction(50, 75);
        f = f.negate();
        Assert.assertEquals(-2, f.getNumeratorAsInt());
        Assert.assertEquals(3, f.getDenominatorAsInt());

        f = new BigFraction(-50, 75);
        f = f.negate();
        Assert.assertEquals(2, f.getNumeratorAsInt());
        Assert.assertEquals(3, f.getDenominatorAsInt());

        
        f = new BigFraction(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
        f = f.negate();
        Assert.assertEquals(Integer.MIN_VALUE + 2, f.getNumeratorAsInt());
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());

    }

// org.apache.commons.math3.fraction.BigFractionTest::testAdd
    public void testAdd() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(1, 1, a.add(a));
        assertFraction(7, 6, a.add(b));
        assertFraction(7, 6, b.add(a));
        assertFraction(4, 3, b.add(b));

        BigFraction f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        BigFraction f2 = BigFraction.ONE;
        BigFraction f = f1.add(f2);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(-1, 13 * 13 * 2 * 2);
        f2 = new BigFraction(-2, 13 * 17 * 2);
        f = f1.add(f2);
        Assert.assertEquals(13 * 13 * 17 * 2 * 2, f.getDenominatorAsInt());
        Assert.assertEquals(-17 - 2 * 13 * 2, f.getNumeratorAsInt());

        try {
            f.add((BigFraction) null);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

        
        
        f1 = new BigFraction(1, 32768 * 3);
        f2 = new BigFraction(1, 59049);
        f = f1.add(f2);
        Assert.assertEquals(52451, f.getNumeratorAsInt());
        Assert.assertEquals(1934917632, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, 3);
        f2 = new BigFraction(1, 3);
        f = f1.add(f2);
        Assert.assertEquals(Integer.MIN_VALUE + 1, f.getNumeratorAsInt());
        Assert.assertEquals(3, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(BigInteger.ONE);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f = f.add(BigInteger.ZERO);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(1);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f = f.add(0);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE - 1, 1);
        f = f1.add(1l);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f = f.add(0l);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

    }

// org.apache.commons.math3.fraction.BigFractionTest::testDivide
    public void testDivide() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(1, 1, a.divide(a));
        assertFraction(3, 4, a.divide(b));
        assertFraction(4, 3, b.divide(a));
        assertFraction(1, 1, b.divide(b));

        BigFraction f1 = new BigFraction(3, 5);
        BigFraction f2 = BigFraction.ZERO;
        try {
            f1.divide(f2);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
        }

        f1 = new BigFraction(0, 5);
        f2 = new BigFraction(2, 7);
        BigFraction f = f1.divide(f2);
        Assert.assertSame(BigFraction.ZERO, f);

        f1 = new BigFraction(2, 7);
        f2 = BigFraction.ONE;
        f = f1.divide(f2);
        Assert.assertEquals(2, f.getNumeratorAsInt());
        Assert.assertEquals(7, f.getDenominatorAsInt());

        f1 = new BigFraction(1, Integer.MAX_VALUE);
        f = f1.divide(f1);
        Assert.assertEquals(1, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f2 = new BigFraction(1, Integer.MAX_VALUE);
        f = f1.divide(f2);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        try {
            f.divide((BigFraction) null);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide(BigInteger.valueOf(Integer.MIN_VALUE));
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        Assert.assertEquals(1, f.getNumeratorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide(Integer.MIN_VALUE);
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        Assert.assertEquals(1, f.getNumeratorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f = f1.divide((long) Integer.MIN_VALUE);
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominatorAsInt());
        Assert.assertEquals(1, f.getNumeratorAsInt());

    }

// org.apache.commons.math3.fraction.BigFractionTest::testMultiply
    public void testMultiply() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(1, 4, a.multiply(a));
        assertFraction(1, 3, a.multiply(b));
        assertFraction(1, 3, b.multiply(a));
        assertFraction(4, 9, b.multiply(b));

        BigFraction f1 = new BigFraction(Integer.MAX_VALUE, 1);
        BigFraction f2 = new BigFraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        BigFraction f = f1.multiply(f2);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f = f2.multiply(Integer.MAX_VALUE);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        f = f2.multiply((long) Integer.MAX_VALUE);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

        try {
            f.multiply((BigFraction) null);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

    }

// org.apache.commons.math3.fraction.BigFractionTest::testSubtract
    public void testSubtract() {
        BigFraction a = new BigFraction(1, 2);
        BigFraction b = new BigFraction(2, 3);

        assertFraction(0, 1, a.subtract(a));
        assertFraction(-1, 6, a.subtract(b));
        assertFraction(1, 6, b.subtract(a));
        assertFraction(0, 1, b.subtract(b));

        BigFraction f = new BigFraction(1, 1);
        try {
            f.subtract((BigFraction) null);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException ex) {
        }

        
        
        BigFraction f1 = new BigFraction(1, 32768 * 3);
        BigFraction f2 = new BigFraction(1, 59049);
        f = f1.subtract(f2);
        Assert.assertEquals(-13085, f.getNumeratorAsInt());
        Assert.assertEquals(1934917632, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MIN_VALUE, 3);
        f2 = new BigFraction(1, 3).negate();
        f = f1.subtract(f2);
        Assert.assertEquals(Integer.MIN_VALUE + 1, f.getNumeratorAsInt());
        Assert.assertEquals(3, f.getDenominatorAsInt());

        f1 = new BigFraction(Integer.MAX_VALUE, 1);
        f2 = BigFraction.ONE;
        f = f1.subtract(f2);
        Assert.assertEquals(Integer.MAX_VALUE - 1, f.getNumeratorAsInt());
        Assert.assertEquals(1, f.getDenominatorAsInt());

    }

// org.apache.commons.math3.fraction.BigFractionTest::testBigDecimalValue
    public void testBigDecimalValue() {
        Assert.assertEquals(new BigDecimal(0.5), new BigFraction(1, 2).bigDecimalValue());
        Assert.assertEquals(new BigDecimal("0.0003"), new BigFraction(3, 10000).bigDecimalValue());
        Assert.assertEquals(new BigDecimal("0"), new BigFraction(1, 3).bigDecimalValue(BigDecimal.ROUND_DOWN));
        Assert.assertEquals(new BigDecimal("0.333"), new BigFraction(1, 3).bigDecimalValue(3, BigDecimal.ROUND_DOWN));
    }

// org.apache.commons.math3.fraction.BigFractionTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BigFraction zero = new BigFraction(0, 1);
        BigFraction nullFraction = null;
        Assert.assertTrue(zero.equals(zero));
        Assert.assertFalse(zero.equals(nullFraction));
        Assert.assertFalse(zero.equals(Double.valueOf(0)));
        BigFraction zero2 = new BigFraction(0, 2);
        Assert.assertTrue(zero.equals(zero2));
        Assert.assertEquals(zero.hashCode(), zero2.hashCode());
        BigFraction one = new BigFraction(1, 1);
        Assert.assertFalse((one.equals(zero) || zero.equals(one)));
        Assert.assertTrue(one.equals(BigFraction.ONE));
    }

// org.apache.commons.math3.fraction.BigFractionTest::testGetReducedFraction
    public void testGetReducedFraction() {
        BigFraction threeFourths = new BigFraction(3, 4);
        Assert.assertTrue(threeFourths.equals(BigFraction.getReducedFraction(6, 8)));
        Assert.assertTrue(BigFraction.ZERO.equals(BigFraction.getReducedFraction(0, -1)));
        try {
            BigFraction.getReducedFraction(1, 0);
            Assert.fail("expecting ZeroException");
        } catch (ZeroException ex) {
            
        }
        Assert.assertEquals(BigFraction.getReducedFraction(2, Integer.MIN_VALUE).getNumeratorAsInt(), -1);
        Assert.assertEquals(BigFraction.getReducedFraction(1, -1).getNumeratorAsInt(), -1);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testPercentage
    public void testPercentage() {
        Assert.assertEquals(50.0, new BigFraction(1, 2).percentageValue(), 1.0e-15);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testPow
    public void testPow() {
        Assert.assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(13));
        Assert.assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(13l));
        Assert.assertEquals(new BigFraction(8192, 1594323), new BigFraction(2, 3).pow(BigInteger.valueOf(13l)));
        Assert.assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(0));
        Assert.assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(0l));
        Assert.assertEquals(BigFraction.ONE, new BigFraction(2, 3).pow(BigInteger.valueOf(0l)));
        Assert.assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(-13));
        Assert.assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(-13l));
        Assert.assertEquals(new BigFraction(1594323, 8192), new BigFraction(2, 3).pow(BigInteger.valueOf(-13l)));
    }

// org.apache.commons.math3.fraction.BigFractionTest::testMath340
    public void testMath340() {
        BigFraction fractionA = new BigFraction(0.00131);
        BigFraction fractionB = new BigFraction(.37).reciprocal();
        BigFraction errorResult = fractionA.multiply(fractionB);
        BigFraction correctResult = new BigFraction(fractionA.getNumerator().multiply(fractionB.getNumerator()),
                                                    fractionA.getDenominator().multiply(fractionB.getDenominator()));
        Assert.assertEquals(correctResult, errorResult);
    }

// org.apache.commons.math3.fraction.BigFractionTest::testSerial
    public void testSerial() throws FractionConversionException {
        BigFraction[] fractions = {
            new BigFraction(3, 4), BigFraction.ONE, BigFraction.ZERO,
            new BigFraction(17), new BigFraction(FastMath.PI, 1000),
            new BigFraction(-5, 2)
        };
        for (BigFraction fraction : fractions) {
            Assert.assertEquals(fraction, TestUtils.serializeAndRecover(fraction));
        }
    }

// org.apache.commons.math3.fraction.FractionFieldTest::testZero
    public void testZero() {
        Assert.assertEquals(Fraction.ZERO, FractionField.getInstance().getZero());
    }

// org.apache.commons.math3.fraction.FractionFieldTest::testOne
    public void testOne() {
        Assert.assertEquals(Fraction.ONE, FractionField.getInstance().getOne());
    }

// org.apache.commons.math3.fraction.FractionFieldTest::testSerial
    public void testSerial() {
        
        FractionField field = FractionField.getInstance();
        Assert.assertTrue(field == TestUtils.serializeAndRecover(field));
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testFormat
    public void testFormat() {
        Fraction c = new Fraction(1, 2);
        String expected = "1 / 2";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testFormatNegative
    public void testFormatNegative() {
        Fraction c = new Fraction(-1, 2);
        String expected = "-1 / 2";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testFormatZero
    public void testFormatZero() {
        Fraction c = new Fraction(0, 1);
        String expected = "0 / 1";

        String actual = properFormat.format(c);
        Assert.assertEquals(expected, actual);

        actual = improperFormat.format(c);
        Assert.assertEquals(expected, actual);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testFormatImproper
    public void testFormatImproper() {
        Fraction c = new Fraction(5, 3);

        String actual = properFormat.format(c);
        Assert.assertEquals("1 2 / 3", actual);

        actual = improperFormat.format(c);
        Assert.assertEquals("5 / 3", actual);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testFormatImproperNegative
    public void testFormatImproperNegative() {
        Fraction c = new Fraction(-5, 3);

        String actual = properFormat.format(c);
        Assert.assertEquals("-1 2 / 3", actual);

        actual = improperFormat.format(c);
        Assert.assertEquals("-5 / 3", actual);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParse
    public void testParse() {
        String source = "1 / 2";

        try {
            Fraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());

            c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());
        } catch (MathParseException ex) {
            Assert.fail(ex.getMessage());
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseInteger
    public void testParseInteger() {
        String source = "10";
        {
            Fraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(10, c.getNumerator());
            Assert.assertEquals(1, c.getDenominator());
        }
        {
            Fraction c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(10, c.getNumerator());
            Assert.assertEquals(1, c.getDenominator());
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseOne1
    public void testParseOne1() {
        String source = "1 / 1";
        Fraction c = properFormat.parse(source);
        Assert.assertNotNull(c);
        Assert.assertEquals(1, c.getNumerator());
        Assert.assertEquals(1, c.getDenominator());
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseOne2
    public void testParseOne2() {
        String source = "10 / 10";
        Fraction c = properFormat.parse(source);
        Assert.assertNotNull(c);
        Assert.assertEquals(1, c.getNumerator());
        Assert.assertEquals(1, c.getDenominator());
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseZero1
    public void testParseZero1() {
        String source = "0 / 1";
        Fraction c = properFormat.parse(source);
        Assert.assertNotNull(c);
        Assert.assertEquals(0, c.getNumerator());
        Assert.assertEquals(1, c.getDenominator());
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseZero2
    public void testParseZero2() {
        String source = "-0 / 1";
        Fraction c = properFormat.parse(source);
        Assert.assertNotNull(c);
        Assert.assertEquals(0, c.getNumerator());
        Assert.assertEquals(1, c.getDenominator());
        
        Assert.assertEquals(Double.POSITIVE_INFINITY, 1d / c.doubleValue(), 0);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseInvalid
    public void testParseInvalid() {
        String source = "a";
        String msg = "should not be able to parse '10 / a'.";
        try {
            properFormat.parse(source);
            Assert.fail(msg);
        } catch (MathParseException ex) {
            
        }
        try {
            improperFormat.parse(source);
            Assert.fail(msg);
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseInvalidDenominator
    public void testParseInvalidDenominator() {
        String source = "10 / a";
        String msg = "should not be able to parse '10 / a'.";
        try {
            properFormat.parse(source);
            Assert.fail(msg);
        } catch (MathParseException ex) {
            
        }
        try {
            improperFormat.parse(source);
            Assert.fail(msg);
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseNegative
    public void testParseNegative() {

        {
            String source = "-1 / 2";
            Fraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());

            c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());

            source = "1 / -2";
            c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());

            c = improperFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-1, c.getNumerator());
            Assert.assertEquals(2, c.getDenominator());
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseProper
    public void testParseProper() {
        String source = "1 2 / 3";

        {
            Fraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(5, c.getNumerator());
            Assert.assertEquals(3, c.getDenominator());
        }

        try {
            improperFormat.parse(source);
            Assert.fail("invalid improper fraction.");
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseProperNegative
    public void testParseProperNegative() {
        String source = "-1 2 / 3";
        {
            Fraction c = properFormat.parse(source);
            Assert.assertNotNull(c);
            Assert.assertEquals(-5, c.getNumerator());
            Assert.assertEquals(3, c.getDenominator());
        }

        try {
            improperFormat.parse(source);
            Assert.fail("invalid improper fraction.");
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testParseProperInvalidMinus
    public void testParseProperInvalidMinus() {
        String source = "2 -2 / 3";
        try {
            properFormat.parse(source);
            Assert.fail("invalid minus in improper fraction.");
        } catch (MathParseException ex) {
            
        }
        source = "2 2 / -3";
        try {
            properFormat.parse(source);
            Assert.fail("invalid minus in improper fraction.");
        } catch (MathParseException ex) {
            
        }
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testNumeratorFormat
    public void testNumeratorFormat() {
        NumberFormat old = properFormat.getNumeratorFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        properFormat.setNumeratorFormat(nf);
        Assert.assertEquals(nf, properFormat.getNumeratorFormat());
        properFormat.setNumeratorFormat(old);

        old = improperFormat.getNumeratorFormat();
        nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        improperFormat.setNumeratorFormat(nf);
        Assert.assertEquals(nf, improperFormat.getNumeratorFormat());
        improperFormat.setNumeratorFormat(old);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testDenominatorFormat
    public void testDenominatorFormat() {
        NumberFormat old = properFormat.getDenominatorFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        properFormat.setDenominatorFormat(nf);
        Assert.assertEquals(nf, properFormat.getDenominatorFormat());
        properFormat.setDenominatorFormat(old);

        old = improperFormat.getDenominatorFormat();
        nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        improperFormat.setDenominatorFormat(nf);
        Assert.assertEquals(nf, improperFormat.getDenominatorFormat());
        improperFormat.setDenominatorFormat(old);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testWholeFormat
    public void testWholeFormat() {
        ProperFractionFormat format = (ProperFractionFormat)properFormat;

        NumberFormat old = format.getWholeFormat();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setParseIntegerOnly(true);
        format.setWholeFormat(nf);
        Assert.assertEquals(nf, format.getWholeFormat());
        format.setWholeFormat(old);
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testLongFormat
    public void testLongFormat() {
        Assert.assertEquals("10 / 1", improperFormat.format(10l));
    }

// org.apache.commons.math3.fraction.FractionFormatTest::testDoubleFormat
    public void testDoubleFormat() {
        Assert.assertEquals("355 / 113", improperFormat.format(FastMath.PI));
    }

// org.apache.commons.math3.fraction.FractionTest::testConstructor
    public void testConstructor() {
        assertFraction(0, 1, new Fraction(0, 1));
        assertFraction(0, 1, new Fraction(0, 2));
        assertFraction(0, 1, new Fraction(0, -1));
        assertFraction(1, 2, new Fraction(1, 2));
        assertFraction(1, 2, new Fraction(2, 4));
        assertFraction(-1, 2, new Fraction(-1, 2));
        assertFraction(-1, 2, new Fraction(1, -2));
        assertFraction(-1, 2, new Fraction(-2, 4));
        assertFraction(-1, 2, new Fraction(2, -4));

        
        try {
            new Fraction(Integer.MIN_VALUE, -1);
            Assert.fail();
        } catch (MathArithmeticException ex) {
            
        }
        try {
            new Fraction(1, Integer.MIN_VALUE);
            Assert.fail();
        } catch (MathArithmeticException ex) {
            
        }

        assertFraction(0, 1, new Fraction(0.00000000000001));
        assertFraction(2, 5, new Fraction(0.40000000000001));
        assertFraction(15, 1, new Fraction(15.0000000000001));
    }

// org.apache.commons.math3.fraction.FractionTest::testGoldenRatio
    public void testGoldenRatio() {
        
        new Fraction((1 + FastMath.sqrt(5)) / 2, 1.0e-12, 25);
    }

// org.apache.commons.math3.fraction.FractionTest::testDoubleConstructor
    public void testDoubleConstructor() throws ConvergenceException  {
        assertFraction(1, 2, new Fraction((double)1 / (double)2));
        assertFraction(1, 3, new Fraction((double)1 / (double)3));
        assertFraction(2, 3, new Fraction((double)2 / (double)3));
        assertFraction(1, 4, new Fraction((double)1 / (double)4));
        assertFraction(3, 4, new Fraction((double)3 / (double)4));
        assertFraction(1, 5, new Fraction((double)1 / (double)5));
        assertFraction(2, 5, new Fraction((double)2 / (double)5));
        assertFraction(3, 5, new Fraction((double)3 / (double)5));
        assertFraction(4, 5, new Fraction((double)4 / (double)5));
        assertFraction(1, 6, new Fraction((double)1 / (double)6));
        assertFraction(5, 6, new Fraction((double)5 / (double)6));
        assertFraction(1, 7, new Fraction((double)1 / (double)7));
        assertFraction(2, 7, new Fraction((double)2 / (double)7));
        assertFraction(3, 7, new Fraction((double)3 / (double)7));
        assertFraction(4, 7, new Fraction((double)4 / (double)7));
        assertFraction(5, 7, new Fraction((double)5 / (double)7));
        assertFraction(6, 7, new Fraction((double)6 / (double)7));
        assertFraction(1, 8, new Fraction((double)1 / (double)8));
        assertFraction(3, 8, new Fraction((double)3 / (double)8));
        assertFraction(5, 8, new Fraction((double)5 / (double)8));
        assertFraction(7, 8, new Fraction((double)7 / (double)8));
        assertFraction(1, 9, new Fraction((double)1 / (double)9));
        assertFraction(2, 9, new Fraction((double)2 / (double)9));
        assertFraction(4, 9, new Fraction((double)4 / (double)9));
        assertFraction(5, 9, new Fraction((double)5 / (double)9));
        assertFraction(7, 9, new Fraction((double)7 / (double)9));
        assertFraction(8, 9, new Fraction((double)8 / (double)9));
        assertFraction(1, 10, new Fraction((double)1 / (double)10));
        assertFraction(3, 10, new Fraction((double)3 / (double)10));
        assertFraction(7, 10, new Fraction((double)7 / (double)10));
        assertFraction(9, 10, new Fraction((double)9 / (double)10));
        assertFraction(1, 11, new Fraction((double)1 / (double)11));
        assertFraction(2, 11, new Fraction((double)2 / (double)11));
        assertFraction(3, 11, new Fraction((double)3 / (double)11));
        assertFraction(4, 11, new Fraction((double)4 / (double)11));
        assertFraction(5, 11, new Fraction((double)5 / (double)11));
        assertFraction(6, 11, new Fraction((double)6 / (double)11));
        assertFraction(7, 11, new Fraction((double)7 / (double)11));
        assertFraction(8, 11, new Fraction((double)8 / (double)11));
        assertFraction(9, 11, new Fraction((double)9 / (double)11));
        assertFraction(10, 11, new Fraction((double)10 / (double)11));
    }

// org.apache.commons.math3.fraction.FractionTest::testDigitLimitConstructor
    public void testDigitLimitConstructor() throws ConvergenceException  {
        assertFraction(2, 5, new Fraction(0.4,   9));
        assertFraction(2, 5, new Fraction(0.4,  99));
        assertFraction(2, 5, new Fraction(0.4, 999));

        assertFraction(3, 5,      new Fraction(0.6152,    9));
        assertFraction(8, 13,     new Fraction(0.6152,   99));
        assertFraction(510, 829,  new Fraction(0.6152,  999));
        assertFraction(769, 1250, new Fraction(0.6152, 9999));
    }

// org.apache.commons.math3.fraction.FractionTest::testIntegerOverflow
    public void testIntegerOverflow() {
        checkIntegerOverflow(0.75000000001455192);
        checkIntegerOverflow(1.0e10);
        checkIntegerOverflow(-1.0e10);
        checkIntegerOverflow(-43979.60679604749);
    }

// org.apache.commons.math3.fraction.FractionTest::testEpsilonLimitConstructor
    public void testEpsilonLimitConstructor() throws ConvergenceException  {
        assertFraction(2, 5, new Fraction(0.4, 1.0e-5, 100));

        assertFraction(3, 5,      new Fraction(0.6152, 0.02, 100));
        assertFraction(8, 13,     new Fraction(0.6152, 1.0e-3, 100));
        assertFraction(251, 408,  new Fraction(0.6152, 1.0e-4, 100));
        assertFraction(251, 408,  new Fraction(0.6152, 1.0e-5, 100));
        assertFraction(510, 829,  new Fraction(0.6152, 1.0e-6, 100));
        assertFraction(769, 1250, new Fraction(0.6152, 1.0e-7, 100));
    }

// org.apache.commons.math3.fraction.FractionTest::testCompareTo
    public void testCompareTo() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);
        Fraction third = new Fraction(1, 2);

        Assert.assertEquals(0, first.compareTo(first));
        Assert.assertEquals(0, first.compareTo(third));
        Assert.assertEquals(1, first.compareTo(second));
        Assert.assertEquals(-1, second.compareTo(first));

        
        
        
        Fraction pi1 = new Fraction(1068966896, 340262731);
        Fraction pi2 = new Fraction( 411557987, 131002976);
        Assert.assertEquals(-1, pi1.compareTo(pi2));
        Assert.assertEquals( 1, pi2.compareTo(pi1));
        Assert.assertEquals(0.0, pi1.doubleValue() - pi2.doubleValue(), 1.0e-20);
    }

// org.apache.commons.math3.fraction.FractionTest::testDoubleValue
    public void testDoubleValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);

        Assert.assertEquals(0.5, first.doubleValue(), 0.0);
        Assert.assertEquals(1.0 / 3.0, second.doubleValue(), 0.0);
    }

// org.apache.commons.math3.fraction.FractionTest::testFloatValue
    public void testFloatValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(1, 3);

        Assert.assertEquals(0.5f, first.floatValue(), 0.0f);
        Assert.assertEquals((float)(1.0 / 3.0), second.floatValue(), 0.0f);
    }

// org.apache.commons.math3.fraction.FractionTest::testIntValue
    public void testIntValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(3, 2);

        Assert.assertEquals(0, first.intValue());
        Assert.assertEquals(1, second.intValue());
    }

// org.apache.commons.math3.fraction.FractionTest::testLongValue
    public void testLongValue() {
        Fraction first = new Fraction(1, 2);
        Fraction second = new Fraction(3, 2);

        Assert.assertEquals(0L, first.longValue());
        Assert.assertEquals(1L, second.longValue());
    }

// org.apache.commons.math3.fraction.FractionTest::testConstructorDouble
    public void testConstructorDouble() {
        assertFraction(1, 2, new Fraction(0.5));
        assertFraction(1, 3, new Fraction(1.0 / 3.0));
        assertFraction(17, 100, new Fraction(17.0 / 100.0));
        assertFraction(317, 100, new Fraction(317.0 / 100.0));
        assertFraction(-1, 2, new Fraction(-0.5));
        assertFraction(-1, 3, new Fraction(-1.0 / 3.0));
        assertFraction(-17, 100, new Fraction(17.0 / -100.0));
        assertFraction(-317, 100, new Fraction(-317.0 / 100.0));
    }

// org.apache.commons.math3.fraction.FractionTest::testAbs
    public void testAbs() {
        Fraction a = new Fraction(10, 21);
        Fraction b = new Fraction(-10, 21);
        Fraction c = new Fraction(10, -21);

        assertFraction(10, 21, a.abs());
        assertFraction(10, 21, b.abs());
        assertFraction(10, 21, c.abs());
    }

// org.apache.commons.math3.fraction.FractionTest::testPercentage
    public void testPercentage() {
        Assert.assertEquals(50.0, new Fraction(1, 2).percentageValue(), 1.0e-15);
    }

// org.apache.commons.math3.fraction.FractionTest::testMath835
    public void testMath835() {
        final int numer = Integer.MAX_VALUE / 99;
        final int denom = 1;
        final double percentage = 100 * ((double) numer) / denom;
        final Fraction frac = new Fraction(numer, denom);
        
        
        Assert.assertEquals(percentage, frac.percentageValue(), Math.ulp(percentage));
    }

// org.apache.commons.math3.fraction.FractionTest::testReciprocal
    public void testReciprocal() {
        Fraction f = null;

        f = new Fraction(50, 75);
        f = f.reciprocal();
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(2, f.getDenominator());

        f = new Fraction(4, 3);
        f = f.reciprocal();
        Assert.assertEquals(3, f.getNumerator());
        Assert.assertEquals(4, f.getDenominator());

        f = new Fraction(-15, 47);
        f = f.reciprocal();
        Assert.assertEquals(-47, f.getNumerator());
        Assert.assertEquals(15, f.getDenominator());

        f = new Fraction(0, 3);
        try {
            f = f.reciprocal();
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        
        f = new Fraction(Integer.MAX_VALUE, 1);
        f = f.reciprocal();
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominator());
    }

// org.apache.commons.math3.fraction.FractionTest::testNegate
    public void testNegate() {
        Fraction f = null;

        f = new Fraction(50, 75);
        f = f.negate();
        Assert.assertEquals(-2, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f = new Fraction(-50, 75);
        f = f.negate();
        Assert.assertEquals(2, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        
        f = new Fraction(Integer.MAX_VALUE-1, Integer.MAX_VALUE);
        f = f.negate();
        Assert.assertEquals(Integer.MIN_VALUE+2, f.getNumerator());
        Assert.assertEquals(Integer.MAX_VALUE, f.getDenominator());

        f = new Fraction(Integer.MIN_VALUE, 1);
        try {
            f = f.negate();
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}
    }

// org.apache.commons.math3.fraction.FractionTest::testAdd
    public void testAdd() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);

        assertFraction(1, 1, a.add(a));
        assertFraction(7, 6, a.add(b));
        assertFraction(7, 6, b.add(a));
        assertFraction(4, 3, b.add(b));

        Fraction f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        Fraction f2 = Fraction.ONE;
        Fraction f = f1.add(f2);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
        f = f1.add(1);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f1 = new Fraction(-1, 13*13*2*2);
        f2 = new Fraction(-2, 13*17*2);
        f = f1.add(f2);
        Assert.assertEquals(13*13*17*2*2, f.getDenominator());
        Assert.assertEquals(-17 - 2*13*2, f.getNumerator());

        try {
            f.add(null);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        
        
        f1 = new Fraction(1,32768*3);
        f2 = new Fraction(1,59049);
        f = f1.add(f2);
        Assert.assertEquals(52451, f.getNumerator());
        Assert.assertEquals(1934917632, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, 3);
        f2 = new Fraction(1,3);
        f = f1.add(f2);
        Assert.assertEquals(Integer.MIN_VALUE+1, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f1 = new Fraction(Integer.MAX_VALUE - 1, 1);
        f2 = Fraction.ONE;
        f = f1.add(f2);
        Assert.assertEquals(Integer.MAX_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        try {
            f = f.add(Fraction.ONE); 
            Assert.fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}

        
        f1 = new Fraction(Integer.MIN_VALUE, 5);
        f2 = new Fraction(-1,5);
        try {
            f = f1.add(f2); 
            Assert.fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(-Integer.MAX_VALUE, 1);
            f = f.add(f);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(-Integer.MAX_VALUE, 1);
            f = f.add(f);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(3,327680);
        f2 = new Fraction(2,59049);
        try {
            f = f1.add(f2); 
            Assert.fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}
    }

// org.apache.commons.math3.fraction.FractionTest::testDivide
    public void testDivide() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);

        assertFraction(1, 1, a.divide(a));
        assertFraction(3, 4, a.divide(b));
        assertFraction(4, 3, b.divide(a));
        assertFraction(1, 1, b.divide(b));

        Fraction f1 = new Fraction(3, 5);
        Fraction f2 = Fraction.ZERO;
        try {
            f1.divide(f2);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(0, 5);
        f2 = new Fraction(2, 7);
        Fraction f = f1.divide(f2);
        Assert.assertSame(Fraction.ZERO, f);

        f1 = new Fraction(2, 7);
        f2 = Fraction.ONE;
        f = f1.divide(f2);
        Assert.assertEquals(2, f.getNumerator());
        Assert.assertEquals(7, f.getDenominator());

        f1 = new Fraction(1, Integer.MAX_VALUE);
        f = f1.divide(f1);
        Assert.assertEquals(1, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        f2 = new Fraction(1, Integer.MAX_VALUE);
        f = f1.divide(f2);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        try {
            f.divide(null);
            Assert.fail("MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        try {
            f1 = new Fraction(1, Integer.MAX_VALUE);
            f = f1.divide(f1.reciprocal());  
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}
        try {
            f1 = new Fraction(1, -Integer.MAX_VALUE);
            f = f1.divide(f1.reciprocal());  
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(6, 35);
        f  = f1.divide(15);
        Assert.assertEquals(2, f.getNumerator());
        Assert.assertEquals(175, f.getDenominator());

    }

// org.apache.commons.math3.fraction.FractionTest::testMultiply
    public void testMultiply() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);

        assertFraction(1, 4, a.multiply(a));
        assertFraction(1, 3, a.multiply(b));
        assertFraction(1, 3, b.multiply(a));
        assertFraction(4, 9, b.multiply(b));

        Fraction f1 = new Fraction(Integer.MAX_VALUE, 1);
        Fraction f2 = new Fraction(Integer.MIN_VALUE, Integer.MAX_VALUE);
        Fraction f = f1.multiply(f2);
        Assert.assertEquals(Integer.MIN_VALUE, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        try {
            f.multiply(null);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        f1 = new Fraction(6, 35);
        f  = f1.multiply(15);
        Assert.assertEquals(18, f.getNumerator());
        Assert.assertEquals(7, f.getDenominator());
    }

// org.apache.commons.math3.fraction.FractionTest::testSubtract
    public void testSubtract() {
        Fraction a = new Fraction(1, 2);
        Fraction b = new Fraction(2, 3);

        assertFraction(0, 1, a.subtract(a));
        assertFraction(-1, 6, a.subtract(b));
        assertFraction(1, 6, b.subtract(a));
        assertFraction(0, 1, b.subtract(b));

        Fraction f = new Fraction(1,1);
        try {
            f.subtract(null);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {}

        
        
        Fraction f1 = new Fraction(1,32768*3);
        Fraction f2 = new Fraction(1,59049);
        f = f1.subtract(f2);
        Assert.assertEquals(-13085, f.getNumerator());
        Assert.assertEquals(1934917632, f.getDenominator());

        f1 = new Fraction(Integer.MIN_VALUE, 3);
        f2 = new Fraction(1,3).negate();
        f = f1.subtract(f2);
        Assert.assertEquals(Integer.MIN_VALUE+1, f.getNumerator());
        Assert.assertEquals(3, f.getDenominator());

        f1 = new Fraction(Integer.MAX_VALUE, 1);
        f2 = Fraction.ONE;
        f = f1.subtract(f2);
        Assert.assertEquals(Integer.MAX_VALUE-1, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());
        f = f1.subtract(1);
        Assert.assertEquals(Integer.MAX_VALUE-1, f.getNumerator());
        Assert.assertEquals(1, f.getDenominator());

        try {
            f1 = new Fraction(1, Integer.MAX_VALUE);
            f2 = new Fraction(1, Integer.MAX_VALUE - 1);
            f = f1.subtract(f2);
            Assert.fail("expecting MathArithmeticException");  
        } catch (MathArithmeticException ex) {}

        
        f1 = new Fraction(Integer.MIN_VALUE, 5);
        f2 = new Fraction(1,5);
        try {
            f = f1.subtract(f2); 
            Assert.fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(Integer.MIN_VALUE, 1);
            f = f.subtract(Fraction.ONE);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        try {
            f= new Fraction(Integer.MAX_VALUE, 1);
            f = f.subtract(Fraction.ONE.negate());
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {}

        f1 = new Fraction(3,327680);
        f2 = new Fraction(2,59049);
        try {
            f = f1.subtract(f2); 
            Assert.fail("expecting MathArithmeticException but got: " + f.toString());
        } catch (MathArithmeticException ex) {}
    }

// org.apache.commons.math3.fraction.FractionTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Fraction zero  = new Fraction(0,1);
        Fraction nullFraction = null;
        Assert.assertTrue( zero.equals(zero));
        Assert.assertFalse(zero.equals(nullFraction));
        Assert.assertFalse(zero.equals(Double.valueOf(0)));
        Fraction zero2 = new Fraction(0,2);
        Assert.assertTrue(zero.equals(zero2));
        Assert.assertEquals(zero.hashCode(), zero2.hashCode());
        Fraction one = new Fraction(1,1);
        Assert.assertFalse((one.equals(zero) ||zero.equals(one)));
    }

// org.apache.commons.math3.fraction.FractionTest::testGetReducedFraction
    public void testGetReducedFraction() {
        Fraction threeFourths = new Fraction(3, 4);
        Assert.assertTrue(threeFourths.equals(Fraction.getReducedFraction(6, 8)));
        Assert.assertTrue(Fraction.ZERO.equals(Fraction.getReducedFraction(0, -1)));
        try {
            Fraction.getReducedFraction(1, 0);
            Assert.fail("expecting MathArithmeticException");
        } catch (MathArithmeticException ex) {
            
        }
        Assert.assertEquals(Fraction.getReducedFraction
                (2, Integer.MIN_VALUE).getNumerator(),-1);
        Assert.assertEquals(Fraction.getReducedFraction
                (1, -1).getNumerator(), -1);
    }

// org.apache.commons.math3.fraction.FractionTest::testToString
    public void testToString() {
        Assert.assertEquals("0", new Fraction(0, 3).toString());
        Assert.assertEquals("3", new Fraction(6, 2).toString());
        Assert.assertEquals("2 / 3", new Fraction(18, 27).toString());
    }

// org.apache.commons.math3.fraction.FractionTest::testSerial
    public void testSerial() throws FractionConversionException {
        Fraction[] fractions = {
            new Fraction(3, 4), Fraction.ONE, Fraction.ZERO,
            new Fraction(17), new Fraction(FastMath.PI, 1000),
            new Fraction(-5, 2)
        };
        for (Fraction fraction : fractions) {
            Assert.assertEquals(fraction, TestUtils.serializeAndRecover(fraction));
        }
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testNextGeneration
    public void testNextGeneration() {
        ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.203);

        for (int i=0; i<pop.getPopulationLimit(); i++) {
            pop.addChromosome(new DummyChromosome());
        }

        Population nextGeneration = pop.nextGeneration();

        Assert.assertEquals(20, nextGeneration.getPopulationSize());
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testSetElitismRate
    public void testSetElitismRate() {
        final double rate = 0.25;
        final ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.203);
        pop.setElitismRate(rate);
        Assert.assertEquals(rate, pop.getElitismRate(), 1e-6);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testSetElitismRateTooLow
    public void testSetElitismRateTooLow() {
        final double rate = -0.25;
        final ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.203);
        pop.setElitismRate(rate);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testSetElitismRateTooHigh
    public void testSetElitismRateTooHigh() {
        final double rate = 1.25;
        final ElitisticListPopulation pop = new ElitisticListPopulation(100, 0.203);
        pop.setElitismRate(rate);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testConstructorTooLow
    public void testConstructorTooLow() {
        final double rate = -0.25;
        new ElitisticListPopulation(100, rate);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testConstructorTooHigh
    public void testConstructorTooHigh() {
        final double rate = 1.25;
        new ElitisticListPopulation(100, rate);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testChromosomeListConstructorTooLow
    public void testChromosomeListConstructorTooLow() {
        final List<Chromosome> chromosomes = Collections.emptyList();
        final double rate = -0.25;
        new ElitisticListPopulation(chromosomes, 100, rate);
    }

// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testChromosomeListConstructorTooHigh
    public void testChromosomeListConstructorTooHigh() {
        final List<Chromosome> chromosomes = Collections.emptyList();
        final double rate = 1.25;
        new ElitisticListPopulation(chromosomes, 100, rate);
    }

// org.apache.commons.math3.genetics.FitnessCachingTest::testFitnessCaching
    public void testFitnessCaching() {
        
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE, 
                new BinaryMutation(),
                MUTATION_RATE, 
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        
        Population initial = randomPopulation();
        
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        
        ga.evolve(initial, stopCond);

        int neededCalls =
            POPULATION_SIZE  +
            (NUM_GENERATIONS - 1)  * (int)(POPULATION_SIZE * (1.0 - ELITISM_RATE)) 
            ;
        Assert.assertTrue(fitnessCalls <= neededCalls); 
    }

// org.apache.commons.math3.genetics.FixedElapsedTimeTest::testIsSatisfied
    public void testIsSatisfied() {
        final Population pop = new Population() {
            public void addChromosome(final Chromosome chromosome) {
                
            }
            public Chromosome getFittestChromosome() {
                
                return null;
            }
            public int getPopulationLimit() {
                
                return 0;
            }
            public int getPopulationSize() {
                
                return 0;
            }
            public Population nextGeneration() {
                
                return null;
            }
            public Iterator<Chromosome> iterator() {
                
                return null;
            }
        };

        final long start = System.nanoTime();
        final long duration = 3;
        final FixedElapsedTime tec = new FixedElapsedTime(duration, TimeUnit.SECONDS);

        while (!tec.isSatisfied(pop)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                
            }
        }

        final long end = System.nanoTime();
        final long elapsedTime = end - start;
        final long diff = FastMath.abs(elapsedTime - TimeUnit.SECONDS.toNanos(duration));

        Assert.assertTrue(diff < TimeUnit.MILLISECONDS.toNanos(100));
    }

// org.apache.commons.math3.genetics.GeneticAlgorithmTestBinary::test
    public void test() {
        

        
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE, 
                new BinaryMutation(),
                MUTATION_RATE,
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        Assert.assertEquals(0, ga.getGenerationsEvolved());

        
        Population initial = randomPopulation();
        
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        
        Chromosome bestInitial = initial.getFittestChromosome();

        
        Population finalPopulation = ga.evolve(initial, stopCond);

        
        Chromosome bestFinal = finalPopulation.getFittestChromosome();

        
        

        Assert.assertTrue(bestFinal.compareTo(bestInitial) > 0);
        Assert.assertEquals(NUM_GENERATIONS, ga.getGenerationsEvolved());

    }

// org.apache.commons.math3.genetics.GeneticAlgorithmTestPermutations::test
    public void test() {
        

        
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE,
                new RandomKeyMutation(),
                MUTATION_RATE,
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        
        Population initial = randomPopulation();
        
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        
        Chromosome bestInitial = initial.getFittestChromosome();

        
        Population finalPopulation = ga.evolve(initial, stopCond);

        
        Chromosome bestFinal = finalPopulation.getFittestChromosome();

        
        

        Assert.assertTrue(bestFinal.compareTo(bestInitial) > 0);

        
        
    }

// org.apache.commons.math3.genetics.OrderedCrossoverTest::testCrossover
    public void testCrossover() {
        final Integer[] p1 = new Integer[] { 8, 4, 7, 3, 6, 2, 5, 1, 9, 0 };
        final Integer[] p2 = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        final DummyListChromosome p1c = new DummyListChromosome(p1);
        final DummyListChromosome p2c = new DummyListChromosome(p2);
        
        final CrossoverPolicy cp = new OrderedCrossover<Integer>();

        for (int i = 0; i < 20; i++) {
            final Set<Integer> parentSet1 = new HashSet<Integer>(Arrays.asList(p1));
            final Set<Integer> parentSet2 = new HashSet<Integer>(Arrays.asList(p2));
            
            final ChromosomePair pair = cp.crossover(p1c, p2c);

            final Integer[] c1 = ((DummyListChromosome) pair.getFirst()).getRepresentation().toArray(new Integer[p1.length]);
            final Integer[] c2 = ((DummyListChromosome) pair.getSecond()).getRepresentation().toArray(new Integer[p2.length]);

            Assert.assertNotSame(p1c, pair.getFirst());
            Assert.assertNotSame(p2c, pair.getSecond());
            
            
            for (int j = 0; j < c1.length; j++) {
                Assert.assertTrue(parentSet1.contains(c1[j]));
                parentSet1.remove(c1[j]);
                Assert.assertTrue(parentSet2.contains(c2[j]));
                parentSet2.remove(c2[j]);
            }
        }
    }

// org.apache.commons.math3.genetics.OrderedCrossoverTest::testCrossoverDimensionMismatchException
    public void testCrossoverDimensionMismatchException() {
        final Integer[] p1 = new Integer[] { 1, 0, 1, 0, 0, 1, 0, 1, 1 };
        final Integer[] p2 = new Integer[] { 0, 1, 1, 0, 1 };

        final BinaryChromosome p1c = new DummyBinaryChromosome(p1);
        final BinaryChromosome p2c = new DummyBinaryChromosome(p2);

        final CrossoverPolicy cp = new OrderedCrossover<Integer>();
        cp.crossover(p1c, p2c);
    }

// org.apache.commons.math3.genetics.OrderedCrossoverTest::testCrossoverInvalidFixedLengthChromosomeFirst
    public void testCrossoverInvalidFixedLengthChromosomeFirst() {
        final Integer[] p1 = new Integer[] { 1, 0, 1, 0, 0, 1, 0, 1, 1 };
        final BinaryChromosome p1c = new DummyBinaryChromosome(p1);
        final Chromosome p2c = new Chromosome() {
            public double fitness() {
                
                return 0;
            }
        };

        final CrossoverPolicy cp = new OrderedCrossover<Integer>();
        cp.crossover(p1c, p2c);
    }

// org.apache.commons.math3.genetics.OrderedCrossoverTest::testCrossoverInvalidFixedLengthChromosomeSecond
    public void testCrossoverInvalidFixedLengthChromosomeSecond() {
        final Integer[] p1 = new Integer[] { 1, 0, 1, 0, 0, 1, 0, 1, 1 };
        final BinaryChromosome p2c = new DummyBinaryChromosome(p1);
        final Chromosome p1c = new Chromosome() {
            public double fitness() {
                
                return 0;
            }
        };

        final CrossoverPolicy cp = new OrderedCrossover<Integer>();
        cp.crossover(p1c, p2c);
    }

// org.apache.commons.math3.geometry.euclidean.oned.IntervalTest::testInterval
    public void testInterval() {
        Interval interval = new Interval(2.3, 5.7);
        Assert.assertEquals(3.4, interval.getSize(), 1.0e-10);
        Assert.assertEquals(4.0, interval.getBarycenter(), 1.0e-10);
        Assert.assertEquals(Region.Location.BOUNDARY, interval.checkPoint(2.3, 1.0e-10));
        Assert.assertEquals(Region.Location.BOUNDARY, interval.checkPoint(5.7, 1.0e-10));
        Assert.assertEquals(Region.Location.OUTSIDE,  interval.checkPoint(1.2, 1.0e-10));
        Assert.assertEquals(Region.Location.OUTSIDE,  interval.checkPoint(8.7, 1.0e-10));
        Assert.assertEquals(Region.Location.INSIDE,   interval.checkPoint(3.0, 1.0e-10));
        Assert.assertEquals(2.3, interval.getInf(), 1.0e-10);
        Assert.assertEquals(5.7, interval.getSup(), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.oned.IntervalTest::testTolerance
    public void testTolerance() {
        Interval interval = new Interval(2.3, 5.7);
        Assert.assertEquals(Region.Location.OUTSIDE,  interval.checkPoint(1.2, 1.0));
        Assert.assertEquals(Region.Location.BOUNDARY, interval.checkPoint(1.2, 1.2));
        Assert.assertEquals(Region.Location.OUTSIDE,  interval.checkPoint(8.7, 2.9));
        Assert.assertEquals(Region.Location.BOUNDARY, interval.checkPoint(8.7, 3.1));
        Assert.assertEquals(Region.Location.INSIDE,   interval.checkPoint(3.0, 0.6));
        Assert.assertEquals(Region.Location.BOUNDARY, interval.checkPoint(3.0, 0.8));
    }

// org.apache.commons.math3.geometry.euclidean.oned.IntervalTest::testInfinite
    public void testInfinite() {
        Interval interval = new Interval(9.0, Double.POSITIVE_INFINITY);
        Assert.assertEquals(Region.Location.BOUNDARY, interval.checkPoint(9.0, 1.0e-10));
        Assert.assertEquals(Region.Location.OUTSIDE,  interval.checkPoint(8.4, 1.0e-10));
        for (double e = 1.0; e <= 6.0; e += 1.0) {
            Assert.assertEquals(Region.Location.INSIDE,
                                interval.checkPoint(FastMath.pow(10.0, e), 1.0e-10));
        }
        Assert.assertTrue(Double.isInfinite(interval.getSize()));
        Assert.assertEquals(9.0, interval.getInf(), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(interval.getSup()));

    }

// org.apache.commons.math3.geometry.euclidean.oned.IntervalTest::testSinglePoint
    public void testSinglePoint() {
        Interval interval = new Interval(1.0, 1.0);
        Assert.assertEquals(0.0, interval.getSize(), Precision.SAFE_MIN);
        Assert.assertEquals(1.0, interval.getBarycenter(), Precision.EPSILON);
    }

// org.apache.commons.math3.geometry.euclidean.oned.IntervalsSetTest::testInterval
    public void testInterval() {
        IntervalsSet set = new IntervalsSet(2.3, 5.7);
        Assert.assertEquals(3.4, set.getSize(), 1.0e-10);
        Assert.assertEquals(4.0, ((Vector1D) set.getBarycenter()).getX(), 1.0e-10);
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(new Vector1D(2.3)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(new Vector1D(5.7)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(new Vector1D(1.2)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(new Vector1D(8.7)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(new Vector1D(3.0)));
        Assert.assertEquals(2.3, set.getInf(), 1.0e-10);
        Assert.assertEquals(5.7, set.getSup(), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.oned.IntervalsSetTest::testInfinite
    public void testInfinite() {
        IntervalsSet set = new IntervalsSet(9.0, Double.POSITIVE_INFINITY);
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(new Vector1D(9.0)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(new Vector1D(8.4)));
        for (double e = 1.0; e <= 6.0; e += 1.0) {
            Assert.assertEquals(Region.Location.INSIDE,
                                set.checkPoint(new Vector1D(FastMath.pow(10.0, e))));
        }
        Assert.assertTrue(Double.isInfinite(set.getSize()));
        Assert.assertEquals(9.0, set.getInf(), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(set.getSup()));

        set = (IntervalsSet) new RegionFactory<Euclidean1D>().getComplement(set);
        Assert.assertEquals(9.0, set.getSup(), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(set.getInf()));

    }

// org.apache.commons.math3.geometry.euclidean.oned.IntervalsSetTest::testMultiple
    public void testMultiple() {
        RegionFactory<Euclidean1D> factory = new RegionFactory<Euclidean1D>();
        IntervalsSet set = (IntervalsSet)
        factory.intersection(factory.union(factory.difference(new IntervalsSet(1.0, 6.0),
                                                              new IntervalsSet(3.0, 5.0)),
                                                              new IntervalsSet(9.0, Double.POSITIVE_INFINITY)),
                                                              new IntervalsSet(Double.NEGATIVE_INFINITY, 11.0));
        Assert.assertEquals(5.0, set.getSize(), 1.0e-10);
        Assert.assertEquals(5.9, ((Vector1D) set.getBarycenter()).getX(), 1.0e-10);
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(new Vector1D(0.0)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(new Vector1D(4.0)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(new Vector1D(8.0)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(new Vector1D(12.0)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(new Vector1D(1.2)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(new Vector1D(5.9)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(new Vector1D(9.01)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(new Vector1D(5.0)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(new Vector1D(11.0)));
        Assert.assertEquals( 1.0, set.getInf(), 1.0e-10);
        Assert.assertEquals(11.0, set.getSup(), 1.0e-10);

        List<Interval> list = set.asList();
        Assert.assertEquals(3, list.size());
        Assert.assertEquals( 1.0, list.get(0).getInf(), 1.0e-10);
        Assert.assertEquals( 3.0, list.get(0).getSup(), 1.0e-10);
        Assert.assertEquals( 5.0, list.get(1).getInf(), 1.0e-10);
        Assert.assertEquals( 6.0, list.get(1).getSup(), 1.0e-10);
        Assert.assertEquals( 9.0, list.get(2).getInf(), 1.0e-10);
        Assert.assertEquals(11.0, list.get(2).getSup(), 1.0e-10);

    }

// org.apache.commons.math3.geometry.euclidean.oned.IntervalsSetTest::testSinglePoint
    public void testSinglePoint() {
        IntervalsSet set = new IntervalsSet(1.0, 1.0);
        Assert.assertEquals(0.0, set.getSize(), Precision.SAFE_MIN);
        Assert.assertEquals(1.0, ((Vector1D) set.getBarycenter()).getX(), Precision.EPSILON);
    }

// org.apache.commons.math3.geometry.euclidean.threed.LineTest::testContains
    public void testContains() throws MathIllegalArgumentException, MathArithmeticException {
        Vector3D p1 = new Vector3D(0, 0, 1);
        Line l = new Line(p1, new Vector3D(0, 0, 2));
        Assert.assertTrue(l.contains(p1));
        Assert.assertTrue(l.contains(new Vector3D(1.0, p1, 0.3, l.getDirection())));
        Vector3D u = l.getDirection().orthogonal();
        Vector3D v = Vector3D.crossProduct(l.getDirection(), u);
        for (double alpha = 0; alpha < 2 * FastMath.PI; alpha += 0.3) {
            Assert.assertTrue(! l.contains(p1.add(new Vector3D(FastMath.cos(alpha), u,
                                                               FastMath.sin(alpha), v))));
        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.LineTest::testSimilar
    public void testSimilar() throws MathIllegalArgumentException, MathArithmeticException {
        Vector3D p1  = new Vector3D (1.2, 3.4, -5.8);
        Vector3D p2  = new Vector3D (3.4, -5.8, 1.2);
        Line     lA  = new Line(p1, p2);
        Line     lB  = new Line(p2, p1);
        Assert.assertTrue(lA.isSimilarTo(lB));
        Assert.assertTrue(! lA.isSimilarTo(new Line(p1, p1.add(lA.getDirection().orthogonal()))));
    }

// org.apache.commons.math3.geometry.euclidean.threed.LineTest::testPointDistance
    public void testPointDistance() throws MathIllegalArgumentException {
        Line l = new Line(new Vector3D(0, 1, 1), new Vector3D(0, 2, 2));
        Assert.assertEquals(FastMath.sqrt(3.0 / 2.0), l.distance(new Vector3D(1, 0, 1)), 1.0e-10);
        Assert.assertEquals(0, l.distance(new Vector3D(0, -4, -4)), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.LineTest::testLineDistance
    public void testLineDistance() throws MathIllegalArgumentException {
        Line l = new Line(new Vector3D(0, 1, 1), new Vector3D(0, 2, 2));
        Assert.assertEquals(1.0,
                            l.distance(new Line(new Vector3D(1, 0, 1), new Vector3D(1, 0, 2))),
                            1.0e-10);
        Assert.assertEquals(0.5,
                            l.distance(new Line(new Vector3D(-0.5, 0, 0), new Vector3D(-0.5, -1, -1))),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(l),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(new Line(new Vector3D(0, -4, -4), new Vector3D(0, -5, -5))),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(new Line(new Vector3D(0, -4, -4), new Vector3D(0, -3, -4))),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.distance(new Line(new Vector3D(0, -4, -4), new Vector3D(1, -4, -4))),
                            1.0e-10);
        Assert.assertEquals(FastMath.sqrt(8),
                            l.distance(new Line(new Vector3D(0, -4, 0), new Vector3D(1, -4, 0))),
                            1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.LineTest::testClosest
    public void testClosest() throws MathIllegalArgumentException {
        Line l = new Line(new Vector3D(0, 1, 1), new Vector3D(0, 2, 2));
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line(new Vector3D(1, 0, 1), new Vector3D(1, 0, 2))).distance(new Vector3D(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.5,
                            l.closestPoint(new Line(new Vector3D(-0.5, 0, 0), new Vector3D(-0.5, -1, -1))).distance(new Vector3D(-0.5, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(l).distance(new Vector3D(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line(new Vector3D(0, -4, -4), new Vector3D(0, -5, -5))).distance(new Vector3D(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line(new Vector3D(0, -4, -4), new Vector3D(0, -3, -4))).distance(new Vector3D(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line(new Vector3D(0, -4, -4), new Vector3D(1, -4, -4))).distance(new Vector3D(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.closestPoint(new Line(new Vector3D(0, -4, 0), new Vector3D(1, -4, 0))).distance(new Vector3D(0, -2, -2)),
                            1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.LineTest::testIntersection
    public void testIntersection() throws MathIllegalArgumentException {
        Line l = new Line(new Vector3D(0, 1, 1), new Vector3D(0, 2, 2));
        Assert.assertNull(l.intersection(new Line(new Vector3D(1, 0, 1), new Vector3D(1, 0, 2))));
        Assert.assertNull(l.intersection(new Line(new Vector3D(-0.5, 0, 0), new Vector3D(-0.5, -1, -1))));
        Assert.assertEquals(0.0,
                            l.intersection(l).distance(new Vector3D(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(new Line(new Vector3D(0, -4, -4), new Vector3D(0, -5, -5))).distance(new Vector3D(0, 0, 0)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(new Line(new Vector3D(0, -4, -4), new Vector3D(0, -3, -4))).distance(new Vector3D(0, -4, -4)),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            l.intersection(new Line(new Vector3D(0, -4, -4), new Vector3D(1, -4, -4))).distance(new Vector3D(0, -4, -4)),
                            1.0e-10);
        Assert.assertNull(l.intersection(new Line(new Vector3D(0, -4, 0), new Vector3D(1, -4, 0))));
    }

// org.apache.commons.math3.geometry.euclidean.threed.PlaneTest::testContains
    public void testContains() throws MathArithmeticException {
        Plane p = new Plane(new Vector3D(0, 0, 1), new Vector3D(0, 0, 1));
        Assert.assertTrue(p.contains(new Vector3D(0, 0, 1)));
        Assert.assertTrue(p.contains(new Vector3D(17, -32, 1)));
        Assert.assertTrue(! p.contains(new Vector3D(17, -32, 1.001)));
    }

// org.apache.commons.math3.geometry.euclidean.threed.PlaneTest::testOffset
    public void testOffset() throws MathArithmeticException {
        Vector3D p1 = new Vector3D(1, 1, 1);
        Plane p = new Plane(p1, new Vector3D(0.2, 0, 0));
        Assert.assertEquals(-5.0, p.getOffset(new Vector3D(-4, 0, 0)), 1.0e-10);
        Assert.assertEquals(+5.0, p.getOffset(new Vector3D(6, 10, -12)), 1.0e-10);
        Assert.assertEquals(0.3,
                            p.getOffset(new Vector3D(1.0, p1, 0.3, p.getNormal())),
                            1.0e-10);
        Assert.assertEquals(-0.3,
                            p.getOffset(new Vector3D(1.0, p1, -0.3, p.getNormal())),
                            1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.PlaneTest::testPoint
    public void testPoint() throws MathArithmeticException {
        Plane p = new Plane(new Vector3D(2, -3, 1), new Vector3D(1, 4, 9));
        Assert.assertTrue(p.contains(p.getOrigin()));
    }

// org.apache.commons.math3.geometry.euclidean.threed.PlaneTest::testThreePoints
    public void testThreePoints() throws MathArithmeticException {
        Vector3D p1 = new Vector3D(1.2, 3.4, -5.8);
        Vector3D p2 = new Vector3D(3.4, -5.8, 1.2);
        Vector3D p3 = new Vector3D(-2.0, 4.3, 0.7);
        Plane    p  = new Plane(p1, p2, p3);
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(p.contains(p3));
    }

// org.apache.commons.math3.geometry.euclidean.threed.PlaneTest::testRotate
    public void testRotate() throws MathArithmeticException, MathIllegalArgumentException {
        Vector3D p1 = new Vector3D(1.2, 3.4, -5.8);
        Vector3D p2 = new Vector3D(3.4, -5.8, 1.2);
        Vector3D p3 = new Vector3D(-2.0, 4.3, 0.7);
        Plane    p  = new Plane(p1, p2, p3);
        Vector3D oldNormal = p.getNormal();

        p = p.rotate(p2, new Rotation(p2.subtract(p1), 1.7));
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

        p = p.rotate(p2, new Rotation(oldNormal, 0.1));
        Assert.assertTrue(! p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

        p = p.rotate(p1, new Rotation(oldNormal, 0.1));
        Assert.assertTrue(! p.contains(p1));
        Assert.assertTrue(! p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

    }

// org.apache.commons.math3.geometry.euclidean.threed.PlaneTest::testTranslate
    public void testTranslate() throws MathArithmeticException {
        Vector3D p1 = new Vector3D(1.2, 3.4, -5.8);
        Vector3D p2 = new Vector3D(3.4, -5.8, 1.2);
        Vector3D p3 = new Vector3D(-2.0, 4.3, 0.7);
        Plane    p  = new Plane(p1, p2, p3);

        p = p.translate(new Vector3D(2.0, p.getU(), -1.5, p.getV()));
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(p.contains(p3));

        p = p.translate(new Vector3D(-1.2, p.getNormal()));
        Assert.assertTrue(! p.contains(p1));
        Assert.assertTrue(! p.contains(p2));
        Assert.assertTrue(! p.contains(p3));

        p = p.translate(new Vector3D(+1.2, p.getNormal()));
        Assert.assertTrue(p.contains(p1));
        Assert.assertTrue(p.contains(p2));
        Assert.assertTrue(p.contains(p3));

    }

// org.apache.commons.math3.geometry.euclidean.threed.PlaneTest::testIntersection
    public void testIntersection() throws MathArithmeticException, MathIllegalArgumentException {
        Plane p = new Plane(new Vector3D(1, 2, 3), new Vector3D(-4, 1, -5));
        Line  l = new Line(new Vector3D(0.2, -3.5, 0.7), new Vector3D(1.2, -2.5, -0.3));
        Vector3D point = p.intersection(l);
        Assert.assertTrue(p.contains(point));
        Assert.assertTrue(l.contains(point));
        Assert.assertNull(p.intersection(new Line(new Vector3D(10, 10, 10),
                                                  new Vector3D(10, 10, 10).add(p.getNormal().orthogonal()))));
    }

// org.apache.commons.math3.geometry.euclidean.threed.PlaneTest::testIntersection2
    public void testIntersection2() throws MathArithmeticException {
        Vector3D p1  = new Vector3D (1.2, 3.4, -5.8);
        Vector3D p2  = new Vector3D (3.4, -5.8, 1.2);
        Plane    pA  = new Plane(p1, p2, new Vector3D (-2.0, 4.3, 0.7));
        Plane    pB  = new Plane(p1, new Vector3D (11.4, -3.8, 5.1), p2);
        Line     l   = pA.intersection(pB);
        Assert.assertTrue(l.contains(p1));
        Assert.assertTrue(l.contains(p2));
        Assert.assertNull(pA.intersection(pA));
    }

// org.apache.commons.math3.geometry.euclidean.threed.PlaneTest::testIntersection3
    public void testIntersection3() throws MathArithmeticException {
        Vector3D reference = new Vector3D (1.2, 3.4, -5.8);
        Plane p1 = new Plane(reference, new Vector3D(1, 3, 3));
        Plane p2 = new Plane(reference, new Vector3D(-2, 4, 0));
        Plane p3 = new Plane(reference, new Vector3D(7, 0, -4));
        Vector3D p = Plane.intersection(p1, p2, p3);
        Assert.assertEquals(reference.getX(), p.getX(), 1.0e-10);
        Assert.assertEquals(reference.getY(), p.getY(), 1.0e-10);
        Assert.assertEquals(reference.getZ(), p.getZ(), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.PlaneTest::testSimilar
    public void testSimilar() throws MathArithmeticException {
        Vector3D p1  = new Vector3D (1.2, 3.4, -5.8);
        Vector3D p2  = new Vector3D (3.4, -5.8, 1.2);
        Vector3D p3  = new Vector3D (-2.0, 4.3, 0.7);
        Plane    pA  = new Plane(p1, p2, p3);
        Plane    pB  = new Plane(p1, new Vector3D (11.4, -3.8, 5.1), p2);
        Assert.assertTrue(! pA.isSimilarTo(pB));
        Assert.assertTrue(pA.isSimilarTo(pA));
        Assert.assertTrue(pA.isSimilarTo(new Plane(p1, p3, p2)));
        Vector3D shift = new Vector3D(0.3, pA.getNormal());
        Assert.assertTrue(! pA.isSimilarTo(new Plane(p1.add(shift),
                                                     p3.add(shift),
                                                     p2.add(shift))));
    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testBox
    public void testBox() {
        PolyhedronsSet tree = new PolyhedronsSet(0, 1, 0, 1, 0, 1);
        Assert.assertEquals(1.0, tree.getSize(), 1.0e-10);
        Assert.assertEquals(6.0, tree.getBoundarySize(), 1.0e-10);
        Vector3D barycenter = (Vector3D) tree.getBarycenter();
        Assert.assertEquals(0.5, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(0.5, barycenter.getY(), 1.0e-10);
        Assert.assertEquals(0.5, barycenter.getZ(), 1.0e-10);
        for (double x = -0.25; x < 1.25; x += 0.1) {
            boolean xOK = (x >= 0.0) && (x <= 1.0);
            for (double y = -0.25; y < 1.25; y += 0.1) {
                boolean yOK = (y >= 0.0) && (y <= 1.0);
                for (double z = -0.25; z < 1.25; z += 0.1) {
                    boolean zOK = (z >= 0.0) && (z <= 1.0);
                    Region.Location expected =
                        (xOK && yOK && zOK) ? Region.Location.INSIDE : Region.Location.OUTSIDE;
                    Assert.assertEquals(expected, tree.checkPoint(new Vector3D(x, y, z)));
                }
            }
        }
        checkPoints(Region.Location.BOUNDARY, tree, new Vector3D[] {
            new Vector3D(0.0, 0.5, 0.5),
            new Vector3D(1.0, 0.5, 0.5),
            new Vector3D(0.5, 0.0, 0.5),
            new Vector3D(0.5, 1.0, 0.5),
            new Vector3D(0.5, 0.5, 0.0),
            new Vector3D(0.5, 0.5, 1.0)
        });
        checkPoints(Region.Location.OUTSIDE, tree, new Vector3D[] {
            new Vector3D(0.0, 1.2, 1.2),
            new Vector3D(1.0, 1.2, 1.2),
            new Vector3D(1.2, 0.0, 1.2),
            new Vector3D(1.2, 1.0, 1.2),
            new Vector3D(1.2, 1.2, 0.0),
            new Vector3D(1.2, 1.2, 1.0)
        });
    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testTetrahedron
    public void testTetrahedron() throws MathArithmeticException {
        Vector3D vertex1 = new Vector3D(1, 2, 3);
        Vector3D vertex2 = new Vector3D(2, 2, 4);
        Vector3D vertex3 = new Vector3D(2, 3, 3);
        Vector3D vertex4 = new Vector3D(1, 3, 4);
        @SuppressWarnings("unchecked")
        PolyhedronsSet tree =
            (PolyhedronsSet) new RegionFactory<Euclidean3D>().buildConvex(
                new Plane(vertex3, vertex2, vertex1),
                new Plane(vertex2, vertex3, vertex4),
                new Plane(vertex4, vertex3, vertex1),
                new Plane(vertex1, vertex2, vertex4));
        Assert.assertEquals(1.0 / 3.0, tree.getSize(), 1.0e-10);
        Assert.assertEquals(2.0 * FastMath.sqrt(3.0), tree.getBoundarySize(), 1.0e-10);
        Vector3D barycenter = (Vector3D) tree.getBarycenter();
        Assert.assertEquals(1.5, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(2.5, barycenter.getY(), 1.0e-10);
        Assert.assertEquals(3.5, barycenter.getZ(), 1.0e-10);
        double third = 1.0 / 3.0;
        checkPoints(Region.Location.BOUNDARY, tree, new Vector3D[] {
            vertex1, vertex2, vertex3, vertex4,
            new Vector3D(third, vertex1, third, vertex2, third, vertex3),
            new Vector3D(third, vertex2, third, vertex3, third, vertex4),
            new Vector3D(third, vertex3, third, vertex4, third, vertex1),
            new Vector3D(third, vertex4, third, vertex1, third, vertex2)
        });
        checkPoints(Region.Location.OUTSIDE, tree, new Vector3D[] {
            new Vector3D(1, 2, 4),
            new Vector3D(2, 2, 3),
            new Vector3D(2, 3, 4),
            new Vector3D(1, 3, 3)
        });
    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testIsometry
    public void testIsometry() throws MathArithmeticException, MathIllegalArgumentException {
        Vector3D vertex1 = new Vector3D(1.1, 2.2, 3.3);
        Vector3D vertex2 = new Vector3D(2.0, 2.4, 4.2);
        Vector3D vertex3 = new Vector3D(2.8, 3.3, 3.7);
        Vector3D vertex4 = new Vector3D(1.0, 3.6, 4.5);
        @SuppressWarnings("unchecked")
        PolyhedronsSet tree =
            (PolyhedronsSet) new RegionFactory<Euclidean3D>().buildConvex(
                new Plane(vertex3, vertex2, vertex1),
                new Plane(vertex2, vertex3, vertex4),
                new Plane(vertex4, vertex3, vertex1),
                new Plane(vertex1, vertex2, vertex4));
        Vector3D barycenter = (Vector3D) tree.getBarycenter();
        Vector3D s = new Vector3D(10.2, 4.3, -6.7);
        Vector3D c = new Vector3D(-0.2, 2.1, -3.2);
        Rotation r = new Rotation(new Vector3D(6.2, -4.4, 2.1), 0.12);

        tree = tree.rotate(c, r).translate(s);

        Vector3D newB =
            new Vector3D(1.0, s,
                         1.0, c,
                         1.0, r.applyTo(barycenter.subtract(c)));
        Assert.assertEquals(0.0,
                            newB.subtract(tree.getBarycenter()).getNorm(),
                            1.0e-10);

        final Vector3D[] expectedV = new Vector3D[] {
            new Vector3D(1.0, s,
                         1.0, c,
                         1.0, r.applyTo(vertex1.subtract(c))),
                         new Vector3D(1.0, s,
                                      1.0, c,
                                      1.0, r.applyTo(vertex2.subtract(c))),
                                      new Vector3D(1.0, s,
                                                   1.0, c,
                                                   1.0, r.applyTo(vertex3.subtract(c))),
                                                   new Vector3D(1.0, s,
                                                                1.0, c,
                                                                1.0, r.applyTo(vertex4.subtract(c)))
        };
        tree.getTree(true).visit(new BSPTreeVisitor<Euclidean3D>() {

            public Order visitOrder(BSPTree<Euclidean3D> node) {
                return Order.MINUS_SUB_PLUS;
            }

            public void visitInternalNode(BSPTree<Euclidean3D> node) {
                @SuppressWarnings("unchecked")
                BoundaryAttribute<Euclidean3D> attribute =
                    (BoundaryAttribute<Euclidean3D>) node.getAttribute();
                if (attribute.getPlusOutside() != null) {
                    checkFacet((SubPlane) attribute.getPlusOutside());
                }
                if (attribute.getPlusInside() != null) {
                    checkFacet((SubPlane) attribute.getPlusInside());
                }
            }

            public void visitLeafNode(BSPTree<Euclidean3D> node) {
            }

            private void checkFacet(SubPlane facet) {
                Plane plane = (Plane) facet.getHyperplane();
                Vector2D[][] vertices =
                    ((PolygonsSet) facet.getRemainingRegion()).getVertices();
                Assert.assertEquals(1, vertices.length);
                for (int i = 0; i < vertices[0].length; ++i) {
                    Vector3D v = plane.toSpace(vertices[0][i]);
                    double d = Double.POSITIVE_INFINITY;
                    for (int k = 0; k < expectedV.length; ++k) {
                        d = FastMath.min(d, v.subtract(expectedV[k]).getNorm());
                    }
                    Assert.assertEquals(0, d, 1.0e-10);
                }
            }

        });

    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testBuildBox
    public void testBuildBox() {
        double x = 1.0;
        double y = 2.0;
        double z = 3.0;
        double w = 0.1;
        double l = 1.0;
        PolyhedronsSet tree =
            new PolyhedronsSet(x - l, x + l, y - w, y + w, z - w, z + w);
        Vector3D barycenter = (Vector3D) tree.getBarycenter();
        Assert.assertEquals(x, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(y, barycenter.getY(), 1.0e-10);
        Assert.assertEquals(z, barycenter.getZ(), 1.0e-10);
        Assert.assertEquals(8 * l * w * w, tree.getSize(), 1.0e-10);
        Assert.assertEquals(8 * w * (2 * l + w), tree.getBoundarySize(), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testCross
    public void testCross() {

        double x = 1.0;
        double y = 2.0;
        double z = 3.0;
        double w = 0.1;
        double l = 1.0;
        PolyhedronsSet xBeam =
            new PolyhedronsSet(x - l, x + l, y - w, y + w, z - w, z + w);
        PolyhedronsSet yBeam =
            new PolyhedronsSet(x - w, x + w, y - l, y + l, z - w, z + w);
        PolyhedronsSet zBeam =
            new PolyhedronsSet(x - w, x + w, y - w, y + w, z - l, z + l);
        RegionFactory<Euclidean3D> factory = new RegionFactory<Euclidean3D>();
        PolyhedronsSet tree = (PolyhedronsSet) factory.union(xBeam, factory.union(yBeam, zBeam));
        Vector3D barycenter = (Vector3D) tree.getBarycenter();

        Assert.assertEquals(x, barycenter.getX(), 1.0e-10);
        Assert.assertEquals(y, barycenter.getY(), 1.0e-10);
        Assert.assertEquals(z, barycenter.getZ(), 1.0e-10);
        Assert.assertEquals(8 * w * w * (3 * l - 2 * w), tree.getSize(), 1.0e-10);
        Assert.assertEquals(24 * w * (2 * l - w), tree.getBoundarySize(), 1.0e-10);

    }

// org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest::testIssue780
    public void testIssue780() throws MathArithmeticException {
        float[] coords = {
            1.000000f, -1.000000f, -1.000000f, 
            1.000000f, -1.000000f, 1.000000f, 
            -1.000000f, -1.000000f, 1.000000f, 
            -1.000000f, -1.000000f, -1.000000f, 
            1.000000f, 1.000000f, -1f, 
            0.999999f, 1.000000f, 1.000000f,   
            -1.000000f, 1.000000f, 1.000000f, 
            -1.000000f, 1.000000f, -1.000000f};
        int[] indices = {
            0, 1, 2, 0, 2, 3, 
            4, 7, 6, 4, 6, 5, 
            0, 4, 5, 0, 5, 1, 
            1, 5, 6, 1, 6, 2, 
            2, 6, 7, 2, 7, 3, 
            4, 0, 3, 4, 3, 7};
        ArrayList<SubHyperplane<Euclidean3D>> subHyperplaneList = new ArrayList<SubHyperplane<Euclidean3D>>();
        for (int idx = 0; idx < indices.length; idx += 3) {
            int idxA = indices[idx] * 3;
            int idxB = indices[idx + 1] * 3;
            int idxC = indices[idx + 2] * 3;
            Vector3D v_1 = new Vector3D(coords[idxA], coords[idxA + 1], coords[idxA + 2]);
            Vector3D v_2 = new Vector3D(coords[idxB], coords[idxB + 1], coords[idxB + 2]);
            Vector3D v_3 = new Vector3D(coords[idxC], coords[idxC + 1], coords[idxC + 2]);
            Vector3D[] vertices = {v_1, v_2, v_3};
            Plane polyPlane = new Plane(v_1, v_2, v_3);
            ArrayList<SubHyperplane<Euclidean2D>> lines = new ArrayList<SubHyperplane<Euclidean2D>>();

            Vector2D[] projPts = new Vector2D[vertices.length];
            for (int ptIdx = 0; ptIdx < projPts.length; ptIdx++) {
                projPts[ptIdx] = polyPlane.toSubSpace(vertices[ptIdx]);
            }

            SubLine lineInPlane = null;
            for (int ptIdx = 0; ptIdx < projPts.length; ptIdx++) {
                lineInPlane = new SubLine(projPts[ptIdx], projPts[(ptIdx + 1) % projPts.length]);
                lines.add(lineInPlane);
            }
            Region<Euclidean2D> polyRegion = new PolygonsSet(lines);
            SubPlane polygon = new SubPlane(polyPlane, polyRegion);
            subHyperplaneList.add(polygon);
        }
        PolyhedronsSet polyhedronsSet = new PolyhedronsSet(subHyperplaneList);
        Assert.assertEquals( 8.0, polyhedronsSet.getSize(), 3.0e-6);
        Assert.assertEquals(24.0, polyhedronsSet.getBoundarySize(), 5.0e-6);
    }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testIdentity
  public void testIdentity() {

    Rotation r = Rotation.IDENTITY;
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

    r = new Rotation(-1, 0, 0, 0, false);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

    r = new Rotation(42, 0, 0, 0, true);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_K);
    checkAngle(r.getAngle(), 0);

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testAxisAngle
  public void testAxisAngle() throws MathIllegalArgumentException {

    Rotation r = new Rotation(new Vector3D(10, 10, 10), 2 * FastMath.PI / 3);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_J);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_I);
    double s = 1 / FastMath.sqrt(3);
    checkVector(r.getAxis(), new Vector3D(s, s, s));
    checkAngle(r.getAngle(), 2 * FastMath.PI / 3);

    try {
      new Rotation(new Vector3D(0, 0, 0), 2 * FastMath.PI / 3);
      Assert.fail("an exception should have been thrown");
    } catch (MathIllegalArgumentException e) {
    }

    r = new Rotation(Vector3D.PLUS_K, 1.5 * FastMath.PI);
    checkVector(r.getAxis(), new Vector3D(0, 0, -1));
    checkAngle(r.getAngle(), 0.5 * FastMath.PI);

    r = new Rotation(Vector3D.PLUS_J, FastMath.PI);
    checkVector(r.getAxis(), Vector3D.PLUS_J);
    checkAngle(r.getAngle(), FastMath.PI);

    checkVector(Rotation.IDENTITY.getAxis(), Vector3D.PLUS_I);

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testRevert
  public void testRevert() {
    Rotation r = new Rotation(0.001, 0.36, 0.48, 0.8, true);
    Rotation reverted = r.revert();
    checkRotation(r.applyTo(reverted), 1, 0, 0, 0);
    checkRotation(reverted.applyTo(r), 1, 0, 0, 0);
    Assert.assertEquals(r.getAngle(), reverted.getAngle(), 1.0e-12);
    Assert.assertEquals(-1, Vector3D.dotProduct(r.getAxis(), reverted.getAxis()), 1.0e-12);
  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testVectorOnePair
  public void testVectorOnePair() throws MathArithmeticException {

    Vector3D u = new Vector3D(3, 2, 1);
    Vector3D v = new Vector3D(-4, 2, 2);
    Rotation r = new Rotation(u, v);
    checkVector(r.applyTo(u.scalarMultiply(v.getNorm())), v.scalarMultiply(u.getNorm()));

    checkAngle(new Rotation(u, u.negate()).getAngle(), FastMath.PI);

    try {
        new Rotation(u, Vector3D.ZERO);
        Assert.fail("an exception should have been thrown");
    } catch (MathArithmeticException e) {
        
    }

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testVectorTwoPairs
  public void testVectorTwoPairs() throws MathArithmeticException {

    Vector3D u1 = new Vector3D(3, 0, 0);
    Vector3D u2 = new Vector3D(0, 5, 0);
    Vector3D v1 = new Vector3D(0, 0, 2);
    Vector3D v2 = new Vector3D(-2, 0, 2);
    Rotation r = new Rotation(u1, u2, v1, v2);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.MINUS_I);

    r = new Rotation(u1, u2, u1.negate(), u2.negate());
    Vector3D axis = r.getAxis();
    if (Vector3D.dotProduct(axis, Vector3D.PLUS_K) > 0) {
      checkVector(axis, Vector3D.PLUS_K);
    } else {
      checkVector(axis, Vector3D.MINUS_K);
    }
    checkAngle(r.getAngle(), FastMath.PI);

    double sqrt = FastMath.sqrt(2) / 2;
    r = new Rotation(Vector3D.PLUS_I,  Vector3D.PLUS_J,
                     new Vector3D(0.5, 0.5,  sqrt),
                     new Vector3D(0.5, 0.5, -sqrt));
    checkRotation(r, sqrt, 0.5, 0.5, 0);

    r = new Rotation(u1, u2, u1, Vector3D.crossProduct(u1, u2));
    checkRotation(r, sqrt, -sqrt, 0, 0);

    checkRotation(new Rotation(u1, u2, u1, u2), 1, 0, 0, 0);

    try {
        new Rotation(u1, u2, Vector3D.ZERO, v2);
        Assert.fail("an exception should have been thrown");
    } catch (MathArithmeticException e) {
      
    }

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testMatrix
  public void testMatrix()
    throws NotARotationMatrixException {

    try {
      new Rotation(new double[][] {
                     { 0.0, 1.0, 0.0 },
                     { 1.0, 0.0, 0.0 }
                   }, 1.0e-7);
      Assert.fail("Expecting NotARotationMatrixException");
    } catch (NotARotationMatrixException nrme) {
      
    }

    try {
      new Rotation(new double[][] {
                     {  0.445888,  0.797184, -0.407040 },
                     {  0.821760, -0.184320,  0.539200 },
                     { -0.354816,  0.574912,  0.737280 }
                   }, 1.0e-7);
      Assert.fail("Expecting NotARotationMatrixException");
    } catch (NotARotationMatrixException nrme) {
      
    }

    try {
        new Rotation(new double[][] {
                       {  0.4,  0.8, -0.4 },
                       { -0.4,  0.6,  0.7 },
                       {  0.8, -0.2,  0.5 }
                     }, 1.0e-15);
        Assert.fail("Expecting NotARotationMatrixException");
      } catch (NotARotationMatrixException nrme) {
        
      }

    checkRotation(new Rotation(new double[][] {
                                 {  0.445888,  0.797184, -0.407040 },
                                 { -0.354816,  0.574912,  0.737280 },
                                 {  0.821760, -0.184320,  0.539200 }
                               }, 1.0e-10),
                  0.8, 0.288, 0.384, 0.36);

    checkRotation(new Rotation(new double[][] {
                                 {  0.539200,  0.737280,  0.407040 },
                                 {  0.184320, -0.574912,  0.797184 },
                                 {  0.821760, -0.354816, -0.445888 }
                              }, 1.0e-10),
                  0.36, 0.8, 0.288, 0.384);

    checkRotation(new Rotation(new double[][] {
                                 { -0.445888,  0.797184, -0.407040 },
                                 {  0.354816,  0.574912,  0.737280 },
                                 {  0.821760,  0.184320, -0.539200 }
                               }, 1.0e-10),
                  0.384, 0.36, 0.8, 0.288);

    checkRotation(new Rotation(new double[][] {
                                 { -0.539200,  0.737280,  0.407040 },
                                 { -0.184320, -0.574912,  0.797184 },
                                 {  0.821760,  0.354816,  0.445888 }
                               }, 1.0e-10),
                  0.288, 0.384, 0.36, 0.8);

    double[][] m1 = { { 0.0, 1.0, 0.0 },
                      { 0.0, 0.0, 1.0 },
                      { 1.0, 0.0, 0.0 } };
    Rotation r = new Rotation(m1, 1.0e-7);
    checkVector(r.applyTo(Vector3D.PLUS_I), Vector3D.PLUS_K);
    checkVector(r.applyTo(Vector3D.PLUS_J), Vector3D.PLUS_I);
    checkVector(r.applyTo(Vector3D.PLUS_K), Vector3D.PLUS_J);

    double[][] m2 = { { 0.83203, -0.55012, -0.07139 },
                      { 0.48293,  0.78164, -0.39474 },
                      { 0.27296,  0.29396,  0.91602 } };
    r = new Rotation(m2, 1.0e-12);

    double[][] m3 = r.getMatrix();
    double d00 = m2[0][0] - m3[0][0];
    double d01 = m2[0][1] - m3[0][1];
    double d02 = m2[0][2] - m3[0][2];
    double d10 = m2[1][0] - m3[1][0];
    double d11 = m2[1][1] - m3[1][1];
    double d12 = m2[1][2] - m3[1][2];
    double d20 = m2[2][0] - m3[2][0];
    double d21 = m2[2][1] - m3[2][1];
    double d22 = m2[2][2] - m3[2][2];

    Assert.assertTrue(FastMath.abs(d00) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d01) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d02) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d10) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d11) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d12) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d20) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d21) < 6.0e-6);
    Assert.assertTrue(FastMath.abs(d22) < 6.0e-6);

    Assert.assertTrue(FastMath.abs(d00) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d01) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d02) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d10) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d11) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d12) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d20) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d21) > 4.0e-7);
    Assert.assertTrue(FastMath.abs(d22) > 4.0e-7);

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        double m3tm3 = m3[i][0] * m3[j][0]
                     + m3[i][1] * m3[j][1]
                     + m3[i][2] * m3[j][2];
        if (i == j) {
          Assert.assertTrue(FastMath.abs(m3tm3 - 1.0) < 1.0e-10);
        } else {
          Assert.assertTrue(FastMath.abs(m3tm3) < 1.0e-10);
        }
      }
    }

    checkVector(r.applyTo(Vector3D.PLUS_I),
                new Vector3D(m3[0][0], m3[1][0], m3[2][0]));
    checkVector(r.applyTo(Vector3D.PLUS_J),
                new Vector3D(m3[0][1], m3[1][1], m3[2][1]));
    checkVector(r.applyTo(Vector3D.PLUS_K),
                new Vector3D(m3[0][2], m3[1][2], m3[2][2]));

    double[][] m4 = { { 1.0,  0.0,  0.0 },
                      { 0.0, -1.0,  0.0 },
                      { 0.0,  0.0, -1.0 } };
    r = new Rotation(m4, 1.0e-7);
    checkAngle(r.getAngle(), FastMath.PI);

    try {
      double[][] m5 = { { 0.0, 0.0, 1.0 },
                        { 0.0, 1.0, 0.0 },
                        { 1.0, 0.0, 0.0 } };
      r = new Rotation(m5, 1.0e-7);
      Assert.fail("got " + r + ", should have caught an exception");
    } catch (NotARotationMatrixException e) {
      
    }

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testAngles
  public void testAngles()
    throws CardanEulerSingularityException {

    RotationOrder[] CardanOrders = {
      RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
      RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
    };

    for (int i = 0; i < CardanOrders.length; ++i) {
      for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 0.3) {
        for (double alpha2 = -1.55; alpha2 < 1.55; alpha2 += 0.3) {
          for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 0.3) {
            Rotation r = new Rotation(CardanOrders[i], alpha1, alpha2, alpha3);
            double[] angles = r.getAngles(CardanOrders[i]);
            checkAngle(angles[0], alpha1);
            checkAngle(angles[1], alpha2);
            checkAngle(angles[2], alpha3);
          }
        }
      }
    }

    RotationOrder[] EulerOrders = {
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
          };

    for (int i = 0; i < EulerOrders.length; ++i) {
      for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 0.3) {
        for (double alpha2 = 0.05; alpha2 < 3.1; alpha2 += 0.3) {
          for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 0.3) {
            Rotation r = new Rotation(EulerOrders[i],
                                      alpha1, alpha2, alpha3);
            double[] angles = r.getAngles(EulerOrders[i]);
            checkAngle(angles[0], alpha1);
            checkAngle(angles[1], alpha2);
            checkAngle(angles[2], alpha3);
          }
        }
      }
    }

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testSingularities
  public void testSingularities() {

    RotationOrder[] CardanOrders = {
      RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
      RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
    };

    double[] singularCardanAngle = { FastMath.PI / 2, -FastMath.PI / 2 };
    for (int i = 0; i < CardanOrders.length; ++i) {
      for (int j = 0; j < singularCardanAngle.length; ++j) {
        Rotation r = new Rotation(CardanOrders[i], 0.1, singularCardanAngle[j], 0.3);
        try {
          r.getAngles(CardanOrders[i]);
          Assert.fail("an exception should have been caught");
        } catch (CardanEulerSingularityException cese) {
          
        }
      }
    }

    RotationOrder[] EulerOrders = {
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
          };

    double[] singularEulerAngle = { 0, FastMath.PI };
    for (int i = 0; i < EulerOrders.length; ++i) {
      for (int j = 0; j < singularEulerAngle.length; ++j) {
        Rotation r = new Rotation(EulerOrders[i], 0.1, singularEulerAngle[j], 0.3);
        try {
          r.getAngles(EulerOrders[i]);
          Assert.fail("an exception should have been caught");
        } catch (CardanEulerSingularityException cese) {
          
        }
      }
    }

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testQuaternion
  public void testQuaternion() throws MathIllegalArgumentException {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    double n = 23.5;
    Rotation r2 = new Rotation(n * r1.getQ0(), n * r1.getQ1(),
                               n * r1.getQ2(), n * r1.getQ3(),
                               true);
    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyTo(u), r1.applyTo(u));
        }
      }
    }

    r1 = new Rotation( 0.288,  0.384,  0.36,  0.8, false);
    checkRotation(r1, -r1.getQ0(), -r1.getQ1(), -r1.getQ2(), -r1.getQ3());

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testCompose
  public void testCompose() throws MathIllegalArgumentException {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    Rotation r2 = new Rotation(new Vector3D(-1, 3, 2), 0.3);
    Rotation r3 = r2.applyTo(r1);

    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyTo(r1.applyTo(u)), r3.applyTo(u));
        }
      }
    }

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testComposeInverse
  public void testComposeInverse() throws MathIllegalArgumentException {

    Rotation r1 = new Rotation(new Vector3D(2, -3, 5), 1.7);
    Rotation r2 = new Rotation(new Vector3D(-1, 3, 2), 0.3);
    Rotation r3 = r2.applyInverseTo(r1);

    for (double x = -0.9; x < 0.9; x += 0.2) {
      for (double y = -0.9; y < 0.9; y += 0.2) {
        for (double z = -0.9; z < 0.9; z += 0.2) {
          Vector3D u = new Vector3D(x, y, z);
          checkVector(r2.applyInverseTo(r1.applyTo(u)), r3.applyTo(u));
        }
      }
    }

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testArray
  public void testArray() throws MathIllegalArgumentException {

      Rotation r = new Rotation(new Vector3D(2, -3, 5), 1.7);

      for (double x = -0.9; x < 0.9; x += 0.2) {
          for (double y = -0.9; y < 0.9; y += 0.2) {
              for (double z = -0.9; z < 0.9; z += 0.2) {
                  Vector3D u = new Vector3D(x, y, z);
                  Vector3D v = r.applyTo(u);
                  double[] inOut = new double[] { x, y, z };
                  r.applyTo(inOut, inOut);
                  Assert.assertEquals(v.getX(), inOut[0], 1.0e-10);
                  Assert.assertEquals(v.getY(), inOut[1], 1.0e-10);
                  Assert.assertEquals(v.getZ(), inOut[2], 1.0e-10);
                  r.applyInverseTo(inOut, inOut);
                  Assert.assertEquals(u.getX(), inOut[0], 1.0e-10);
                  Assert.assertEquals(u.getY(), inOut[1], 1.0e-10);
                  Assert.assertEquals(u.getZ(), inOut[2], 1.0e-10);
              }
          }
      }

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testApplyInverseTo
  public void testApplyInverseTo() throws MathIllegalArgumentException {

    Rotation r = new Rotation(new Vector3D(2, -3, 5), 1.7);
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(FastMath.cos(lambda) * FastMath.cos(phi),
                                    FastMath.sin(lambda) * FastMath.cos(phi),
                                    FastMath.sin(phi));
          r.applyInverseTo(r.applyTo(u));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

    r = Rotation.IDENTITY;
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(FastMath.cos(lambda) * FastMath.cos(phi),
                                    FastMath.sin(lambda) * FastMath.cos(phi),
                                    FastMath.sin(phi));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

    r = new Rotation(Vector3D.PLUS_K, FastMath.PI);
    for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
      for (double phi = -1.55; phi < 1.55; phi += 0.2) {
          Vector3D u = new Vector3D(FastMath.cos(lambda) * FastMath.cos(phi),
                                    FastMath.sin(lambda) * FastMath.cos(phi),
                                    FastMath.sin(phi));
          checkVector(u, r.applyInverseTo(r.applyTo(u)));
          checkVector(u, r.applyTo(r.applyInverseTo(u)));
      }
    }

  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testIssue639
  public void testIssue639() throws MathArithmeticException{
      Vector3D u1 = new Vector3D(-1321008684645961.0 /  268435456.0,
                                 -5774608829631843.0 /  268435456.0,
                                 -3822921525525679.0 / 4294967296.0);
      Vector3D u2 =new Vector3D( -5712344449280879.0 /    2097152.0,
                                 -2275058564560979.0 /    1048576.0,
                                  4423475992255071.0 /      65536.0);
      Rotation rot = new Rotation(u1, u2, Vector3D.PLUS_I,Vector3D.PLUS_K);
      Assert.assertEquals( 0.6228370359608200639829222, rot.getQ0(), 1.0e-15);
      Assert.assertEquals( 0.0257707621456498790029987, rot.getQ1(), 1.0e-15);
      Assert.assertEquals(-0.0000000002503012255839931, rot.getQ2(), 1.0e-15);
      Assert.assertEquals(-0.7819270390861109450724902, rot.getQ3(), 1.0e-15);
  }

// org.apache.commons.math3.geometry.euclidean.threed.RotationTest::testIssue801
  public void testIssue801() throws MathArithmeticException {
      Vector3D u1 = new Vector3D(0.9999988431610581, -0.0015210774290851095, 0.0);
      Vector3D u2 = new Vector3D(0.0, 0.0, 1.0);

      Vector3D v1 = new Vector3D(0.9999999999999999, 0.0, 0.0);
      Vector3D v2 = new Vector3D(0.0, 0.0, -1.0);

      Rotation quat = new Rotation(u1, u2, v1, v2);
      double q2 = quat.getQ0() * quat.getQ0() +
                  quat.getQ1() * quat.getQ1() +
                  quat.getQ2() * quat.getQ2() +
                  quat.getQ3() * quat.getQ3();
      Assert.assertEquals(1.0, q2, 1.0e-14);
      Assert.assertEquals(0.0, Vector3D.angle(v1, quat.applyTo(u1)), 1.0e-14);
      Assert.assertEquals(0.0, Vector3D.angle(v2, quat.applyTo(u2)), 1.0e-14);
      
  }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testEndPoints
    public void testEndPoints() throws MathIllegalArgumentException {
        Vector3D p1 = new Vector3D(-1, -7, 2);
        Vector3D p2 = new Vector3D(7, -1, 0);
        Segment segment = new Segment(p1, p2, new Line(p1, p2));
        SubLine sub = new SubLine(segment);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, new Vector3D(-1, -7, 2).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertEquals(0.0, new Vector3D( 7, -1, 0).distance(segments.get(0).getEnd()), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testNoEndPoints
    public void testNoEndPoints() throws MathIllegalArgumentException {
        SubLine wholeLine = new Line(new Vector3D(-1, 7, 2), new Vector3D(7, 1, 0)).wholeLine();
        List<Segment> segments = wholeLine.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getZ()) &&
                          segments.get(0).getStart().getZ() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getZ()) &&
                          segments.get(0).getEnd().getZ() < 0);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testNoSegments
    public void testNoSegments() throws MathIllegalArgumentException {
        SubLine empty = new SubLine(new Line(new Vector3D(-1, -7, 2), new Vector3D(7, -1, 0)),
                                    (IntervalsSet) new RegionFactory<Euclidean1D>().getComplement(new IntervalsSet()));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(0, segments.size());
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testSeveralSegments
    public void testSeveralSegments() throws MathIllegalArgumentException {
        SubLine twoSubs = new SubLine(new Line(new Vector3D(-1, -7, 2), new Vector3D(7, -1, 0)),
                                      (IntervalsSet) new RegionFactory<Euclidean1D>().union(new IntervalsSet(1, 2),
                                                                                            new IntervalsSet(3, 4)));
        List<Segment> segments = twoSubs.getSegments();
        Assert.assertEquals(2, segments.size());
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testHalfInfiniteNeg
    public void testHalfInfiniteNeg() throws MathIllegalArgumentException {
        SubLine empty = new SubLine(new Line(new Vector3D(-1, -7, 2), new Vector3D(7, -1, -2)),
                                    new IntervalsSet(Double.NEGATIVE_INFINITY, 0.0));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getZ()) &&
                          segments.get(0).getStart().getZ() > 0);
        Assert.assertEquals(0.0, new Vector3D(3, -4, 0).distance(segments.get(0).getEnd()), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testHalfInfinitePos
    public void testHalfInfinitePos() throws MathIllegalArgumentException {
        SubLine empty = new SubLine(new Line(new Vector3D(-1, -7, 2), new Vector3D(7, -1, -2)),
                                    new IntervalsSet(0.0, Double.POSITIVE_INFINITY));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, new Vector3D(3, -4, 0).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getZ()) &&
                          segments.get(0).getEnd().getZ() < 0);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionInsideInside
    public void testIntersectionInsideInside() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(3, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 2, 2));
        Assert.assertEquals(0.0, new Vector3D(2, 1, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertEquals(0.0, new Vector3D(2, 1, 1).distance(sub1.intersection(sub2, false)), 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionInsideBoundary
    public void testIntersectionInsideBoundary() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(3, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 1, 1));
        Assert.assertEquals(0.0, new Vector3D(2, 1, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionInsideOutside
    public void testIntersectionInsideOutside() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(3, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 0.5, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionBoundaryBoundary
    public void testIntersectionBoundaryBoundary() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(2, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 1, 1));
        Assert.assertEquals(0.0, new Vector3D(2, 1, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionBoundaryOutside
    public void testIntersectionBoundaryOutside() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(2, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 0.5, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionOutsideOutside
    public void testIntersectionOutsideOutside() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(1.5, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 0, 0), new Vector3D(2, 0.5, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testConstructors
    public void testConstructors() throws DimensionMismatchException {
        double r = FastMath.sqrt(2) /2;
        checkVector(new Vector3D(2, new Vector3D(FastMath.PI / 3, -FastMath.PI / 4)),
                    r, r * FastMath.sqrt(3), -2 * r);
        checkVector(new Vector3D(2, Vector3D.PLUS_I,
                                 -3, Vector3D.MINUS_K),
                    2, 0, 3);
        checkVector(new Vector3D(2, Vector3D.PLUS_I,
                                 5, Vector3D.PLUS_J,
                                 -3, Vector3D.MINUS_K),
                    2, 5, 3);
        checkVector(new Vector3D(2, Vector3D.PLUS_I,
                                 5, Vector3D.PLUS_J,
                                 5, Vector3D.MINUS_J,
                                 -3, Vector3D.MINUS_K),
                    2, 0, 3);
        checkVector(new Vector3D(new double[] { 2,  5,  -3 }),
                    2, 5, -3);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testWrongDimension
    public void testWrongDimension() throws DimensionMismatchException {
        new Vector3D(new double[] { 2,  5 });
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testCoordinates
    public void testCoordinates() {
        Vector3D v = new Vector3D(1, 2, 3);
        Assert.assertTrue(FastMath.abs(v.getX() - 1) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(v.getY() - 2) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(v.getZ() - 3) < 1.0e-12);
        double[] coordinates = v.toArray();
        Assert.assertTrue(FastMath.abs(coordinates[0] - 1) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(coordinates[1] - 2) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(coordinates[2] - 3) < 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testNorm1
    public void testNorm1() {
        Assert.assertEquals(0.0, Vector3D.ZERO.getNorm1(), 0);
        Assert.assertEquals(6.0, new Vector3D(1, -2, 3).getNorm1(), 0);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testNorm
    public void testNorm() {
        Assert.assertEquals(0.0, Vector3D.ZERO.getNorm(), 0);
        Assert.assertEquals(FastMath.sqrt(14), new Vector3D(1, 2, 3).getNorm(), 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testNormInf
    public void testNormInf() {
        Assert.assertEquals(0.0, Vector3D.ZERO.getNormInf(), 0);
        Assert.assertEquals(3.0, new Vector3D(1, -2, 3).getNormInf(), 0);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testDistance1
    public void testDistance1() {
        Vector3D v1 = new Vector3D(1, -2, 3);
        Vector3D v2 = new Vector3D(-4, 2, 0);
        Assert.assertEquals(0.0, Vector3D.distance1(Vector3D.MINUS_I, Vector3D.MINUS_I), 0);
        Assert.assertEquals(12.0, Vector3D.distance1(v1, v2), 1.0e-12);
        Assert.assertEquals(v1.subtract(v2).getNorm1(), Vector3D.distance1(v1, v2), 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testDistance
    public void testDistance() {
        Vector3D v1 = new Vector3D(1, -2, 3);
        Vector3D v2 = new Vector3D(-4, 2, 0);
        Assert.assertEquals(0.0, Vector3D.distance(Vector3D.MINUS_I, Vector3D.MINUS_I), 0);
        Assert.assertEquals(FastMath.sqrt(50), Vector3D.distance(v1, v2), 1.0e-12);
        Assert.assertEquals(v1.subtract(v2).getNorm(), Vector3D.distance(v1, v2), 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testDistanceSq
    public void testDistanceSq() {
        Vector3D v1 = new Vector3D(1, -2, 3);
        Vector3D v2 = new Vector3D(-4, 2, 0);
        Assert.assertEquals(0.0, Vector3D.distanceSq(Vector3D.MINUS_I, Vector3D.MINUS_I), 0);
        Assert.assertEquals(50.0, Vector3D.distanceSq(v1, v2), 1.0e-12);
        Assert.assertEquals(Vector3D.distance(v1, v2) * Vector3D.distance(v1, v2),
                            Vector3D.distanceSq(v1, v2), 1.0e-12);
  }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testDistanceInf
    public void testDistanceInf() {
        Vector3D v1 = new Vector3D(1, -2, 3);
        Vector3D v2 = new Vector3D(-4, 2, 0);
        Assert.assertEquals(0.0, Vector3D.distanceInf(Vector3D.MINUS_I, Vector3D.MINUS_I), 0);
        Assert.assertEquals(5.0, Vector3D.distanceInf(v1, v2), 1.0e-12);
        Assert.assertEquals(v1.subtract(v2).getNormInf(), Vector3D.distanceInf(v1, v2), 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testSubtract
    public void testSubtract() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(-3, -2, -1);
        v1 = v1.subtract(v2);
        checkVector(v1, 4, 4, 4);

        checkVector(v2.subtract(v1), -7, -6, -5);
        checkVector(v2.subtract(3, v1), -15, -14, -13);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testAdd
    public void testAdd() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(-3, -2, -1);
        v1 = v1.add(v2);
        checkVector(v1, -2, 0, 2);

        checkVector(v2.add(v1), -5, -2, 1);
        checkVector(v2.add(3, v1), -9, -2, 5);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testScalarProduct
    public void testScalarProduct() {
        Vector3D v = new Vector3D(1, 2, 3);
        v = v.scalarMultiply(3);
        checkVector(v, 3, 6, 9);

        checkVector(v.scalarMultiply(0.5), 1.5, 3, 4.5);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testVectorialProducts
    public void testVectorialProducts() {
        Vector3D v1 = new Vector3D(2, 1, -4);
        Vector3D v2 = new Vector3D(3, 1, -1);

        Assert.assertTrue(FastMath.abs(Vector3D.dotProduct(v1, v2) - 11) < 1.0e-12);

        Vector3D v3 = Vector3D.crossProduct(v1, v2);
        checkVector(v3, 3, -10, -1);

        Assert.assertTrue(FastMath.abs(Vector3D.dotProduct(v1, v3)) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(Vector3D.dotProduct(v2, v3)) < 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testCrossProductCancellation
    public void testCrossProductCancellation() {
        Vector3D v1 = new Vector3D(9070467121.0, 4535233560.0, 1);
        Vector3D v2 = new Vector3D(9070467123.0, 4535233561.0, 1);
        checkVector(Vector3D.crossProduct(v1, v2), -1, 2, 1);

        double scale    = FastMath.scalb(1.0, 100);
        Vector3D big1   = new Vector3D(scale, v1);
        Vector3D small2 = new Vector3D(1 / scale, v2);
        checkVector(Vector3D.crossProduct(big1, small2), -1, 2, 1);

    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testAngular
    public void testAngular() {
        Assert.assertEquals(0,           Vector3D.PLUS_I.getAlpha(), 1.0e-10);
        Assert.assertEquals(0,           Vector3D.PLUS_I.getDelta(), 1.0e-10);
        Assert.assertEquals(FastMath.PI / 2, Vector3D.PLUS_J.getAlpha(), 1.0e-10);
        Assert.assertEquals(0,           Vector3D.PLUS_J.getDelta(), 1.0e-10);
        Assert.assertEquals(0,           Vector3D.PLUS_K.getAlpha(), 1.0e-10);
        Assert.assertEquals(FastMath.PI / 2, Vector3D.PLUS_K.getDelta(), 1.0e-10);
      
        Vector3D u = new Vector3D(-1, 1, -1);
        Assert.assertEquals(3 * FastMath.PI /4, u.getAlpha(), 1.0e-10);
        Assert.assertEquals(-1.0 / FastMath.sqrt(3), FastMath.sin(u.getDelta()), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testAngularSeparation
    public void testAngularSeparation() throws MathArithmeticException {
        Vector3D v1 = new Vector3D(2, -1, 4);

        Vector3D  k = v1.normalize();
        Vector3D  i = k.orthogonal();
        Vector3D v2 = k.scalarMultiply(FastMath.cos(1.2)).add(i.scalarMultiply(FastMath.sin(1.2)));

        Assert.assertTrue(FastMath.abs(Vector3D.angle(v1, v2) - 1.2) < 1.0e-12);
  }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testNormalize
    public void testNormalize() throws MathArithmeticException {
        Assert.assertEquals(1.0, new Vector3D(5, -4, 2).normalize().getNorm(), 1.0e-12);
        try {
            Vector3D.ZERO.normalize();
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException ae) {
            
        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testOrthogonal
    public void testOrthogonal() throws MathArithmeticException {
        Vector3D v1 = new Vector3D(0.1, 2.5, 1.3);
        Assert.assertEquals(0.0, Vector3D.dotProduct(v1, v1.orthogonal()), 1.0e-12);
        Vector3D v2 = new Vector3D(2.3, -0.003, 7.6);
        Assert.assertEquals(0.0, Vector3D.dotProduct(v2, v2.orthogonal()), 1.0e-12);
        Vector3D v3 = new Vector3D(-1.7, 1.4, 0.2);
        Assert.assertEquals(0.0, Vector3D.dotProduct(v3, v3.orthogonal()), 1.0e-12);
        try {
            new Vector3D(0, 0, 0).orthogonal();
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException ae) {
            
        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testAngle
    public void testAngle() throws MathArithmeticException {
        Assert.assertEquals(0.22572612855273393616,
                            Vector3D.angle(new Vector3D(1, 2, 3), new Vector3D(4, 5, 6)),
                            1.0e-12);
        Assert.assertEquals(7.98595620686106654517199e-8,
                            Vector3D.angle(new Vector3D(1, 2, 3), new Vector3D(2, 4, 6.000001)),
                            1.0e-12);
        Assert.assertEquals(3.14159257373023116985197793156,
                            Vector3D.angle(new Vector3D(1, 2, 3), new Vector3D(-2, -4, -6.000001)),
                            1.0e-12);
        try {
            Vector3D.angle(Vector3D.ZERO, Vector3D.PLUS_I);
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException ae) {
            
        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testAccurateDotProduct
    public void testAccurateDotProduct() {
        
        
        
        Vector3D u1 = new Vector3D(-1321008684645961.0 /  268435456.0,
                                   -5774608829631843.0 /  268435456.0,
                                   -7645843051051357.0 / 8589934592.0);
        Vector3D u2 = new Vector3D(-5712344449280879.0 /    2097152.0,
                                   -4550117129121957.0 /    2097152.0,
                                    8846951984510141.0 /     131072.0);
        double sNaive = u1.getX() * u2.getX() + u1.getY() * u2.getY() + u1.getZ() * u2.getZ();
        double sAccurate = u1.dotProduct(u2);
        Assert.assertEquals(0.0, sNaive, 1.0e-30);
        Assert.assertEquals(-2088690039198397.0 / 1125899906842624.0, sAccurate, 1.0e-16);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testDotProduct
    public void testDotProduct() {
        
        
        Well1024a random = new Well1024a(553267312521321234l);
        for (int i = 0; i < 10000; ++i) {
            double ux = 10000 * random.nextDouble();
            double uy = 10000 * random.nextDouble();
            double uz = 10000 * random.nextDouble();
            double vx = 10000 * random.nextDouble();
            double vy = 10000 * random.nextDouble();
            double vz = 10000 * random.nextDouble();
            double sNaive = ux * vx + uy * vy + uz * vz;
            double sAccurate = new Vector3D(ux, uy, uz).dotProduct(new Vector3D(vx, vy, vz));
            Assert.assertEquals(sNaive, sAccurate, 2.5e-16 * sAccurate);
        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testAccurateCrossProduct
    public void testAccurateCrossProduct() {
        
        
        
        
        
        final Vector3D u1 = new Vector3D(-1321008684645961.0 /   268435456.0,
                                         -5774608829631843.0 /   268435456.0,
                                         -7645843051051357.0 /  8589934592.0);
        final Vector3D u2 = new Vector3D( 1796571811118507.0 /  2147483648.0,
                                          7853468008299307.0 /  2147483648.0,
                                          2599586637357461.0 / 17179869184.0);
        final Vector3D u3 = new Vector3D(12753243807587107.0 / 18446744073709551616.0, 
                                         -2313766922703915.0 / 18446744073709551616.0, 
                                          -227970081415313.0 /   288230376151711744.0);
        Vector3D cNaive = new Vector3D(u1.getY() * u2.getZ() - u1.getZ() * u2.getY(),
                                       u1.getZ() * u2.getX() - u1.getX() * u2.getZ(),
                                       u1.getX() * u2.getY() - u1.getY() * u2.getX());
        Vector3D cAccurate = u1.crossProduct(u2);
        Assert.assertTrue(u3.distance(cNaive) > 2.9 * u3.getNorm());
        Assert.assertEquals(0.0, u3.distance(cAccurate), 1.0e-30 * cAccurate.getNorm());
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testCrossProduct
    public void testCrossProduct() {
        
        
        Well1024a random = new Well1024a(885362227452043214l);
        for (int i = 0; i < 10000; ++i) {
            double ux = 10000 * random.nextDouble();
            double uy = 10000 * random.nextDouble();
            double uz = 10000 * random.nextDouble();
            double vx = 10000 * random.nextDouble();
            double vy = 10000 * random.nextDouble();
            double vz = 10000 * random.nextDouble();
            Vector3D cNaive = new Vector3D(uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx);
            Vector3D cAccurate = new Vector3D(ux, uy, uz).crossProduct(new Vector3D(vx, vy, vz));
            Assert.assertEquals(0.0, cAccurate.distance(cNaive), 6.0e-15 * cAccurate.getNorm());
        }
    }

// org.apache.commons.math3.geometry.euclidean.twod.LineTest::testContains
    public void testContains() {
        Line l = new Line(new Vector2D(0, 1), new Vector2D(1, 2));
        Assert.assertTrue(l.contains(new Vector2D(0, 1)));
        Assert.assertTrue(l.contains(new Vector2D(1, 2)));
        Assert.assertTrue(l.contains(new Vector2D(7, 8)));
        Assert.assertTrue(! l.contains(new Vector2D(8, 7)));
    }

// org.apache.commons.math3.geometry.euclidean.twod.LineTest::testAbscissa
    public void testAbscissa() {
        Line l = new Line(new Vector2D(2, 1), new Vector2D(-2, -2));
        Assert.assertEquals(0.0,
                            (l.toSubSpace(new Vector2D(-3,  4))).getX(),
                            1.0e-10);
        Assert.assertEquals(0.0,
                            (l.toSubSpace(new Vector2D( 3, -4))).getX(),
                            1.0e-10);
        Assert.assertEquals(-5.0,
                            (l.toSubSpace(new Vector2D( 7, -1))).getX(),
                            1.0e-10);
        Assert.assertEquals( 5.0,
                             (l.toSubSpace(new Vector2D(-1, -7))).getX(),
                             1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.twod.LineTest::testOffset
    public void testOffset() {
        Line l = new Line(new Vector2D(2, 1), new Vector2D(-2, -2));
        Assert.assertEquals(-5.0, l.getOffset(new Vector2D(5, -3)), 1.0e-10);
        Assert.assertEquals(+5.0, l.getOffset(new Vector2D(-5, 2)), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.twod.LineTest::testDistance
    public void testDistance() {
        Line l = new Line(new Vector2D(2, 1), new Vector2D(-2, -2));
        Assert.assertEquals(+5.0, l.distance(new Vector2D(5, -3)), 1.0e-10);
        Assert.assertEquals(+5.0, l.distance(new Vector2D(-5, 2)), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.twod.LineTest::testPointAt
    public void testPointAt() {
        Line l = new Line(new Vector2D(2, 1), new Vector2D(-2, -2));
        for (double a = -2.0; a < 2.0; a += 0.2) {
            Vector1D pA = new Vector1D(a);
            Vector2D point = l.toSpace(pA);
            Assert.assertEquals(a, (l.toSubSpace(point)).getX(), 1.0e-10);
            Assert.assertEquals(0.0, l.getOffset(point),   1.0e-10);
            for (double o = -2.0; o < 2.0; o += 0.2) {
                point = l.getPointAt(pA, o);
                Assert.assertEquals(a, (l.toSubSpace(point)).getX(), 1.0e-10);
                Assert.assertEquals(o, l.getOffset(point),   1.0e-10);
            }
        }
    }

// org.apache.commons.math3.geometry.euclidean.twod.LineTest::testOriginOffset
    public void testOriginOffset() {
        Line l1 = new Line(new Vector2D(0, 1), new Vector2D(1, 2));
        Assert.assertEquals(FastMath.sqrt(0.5), l1.getOriginOffset(), 1.0e-10);
        Line l2 = new Line(new Vector2D(1, 2), new Vector2D(0, 1));
        Assert.assertEquals(-FastMath.sqrt(0.5), l2.getOriginOffset(), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.twod.LineTest::testParallel
    public void testParallel() {
        Line l1 = new Line(new Vector2D(0, 1), new Vector2D(1, 2));
        Line l2 = new Line(new Vector2D(2, 2), new Vector2D(3, 3));
        Assert.assertTrue(l1.isParallelTo(l2));
        Line l3 = new Line(new Vector2D(1, 0), new Vector2D(0.5, -0.5));
        Assert.assertTrue(l1.isParallelTo(l3));
        Line l4 = new Line(new Vector2D(1, 0), new Vector2D(0.5, -0.51));
        Assert.assertTrue(! l1.isParallelTo(l4));
    }

// org.apache.commons.math3.geometry.euclidean.twod.LineTest::testTransform
    public void testTransform() throws MathIllegalArgumentException {

        Line l1 = new Line(new Vector2D(1.0 ,1.0), new Vector2D(4.0 ,1.0));
        Transform<Euclidean2D, Euclidean1D> t1 =
            Line.getTransform(new AffineTransform(0.0, 0.5, -1.0, 0.0, 1.0, 1.5));
        Assert.assertEquals(0.5 * FastMath.PI,
                            ((Line) t1.apply(l1)).getAngle(),
                            1.0e-10);

        Line l2 = new Line(new Vector2D(0.0, 0.0), new Vector2D(1.0, 1.0));
        Transform<Euclidean2D, Euclidean1D> t2 =
            Line.getTransform(new AffineTransform(0.0, 0.5, -1.0, 0.0, 1.0, 1.5));
        Assert.assertEquals(FastMath.atan2(1.0, -2.0),
                            ((Line) t2.apply(l2)).getAngle(),
                            1.0e-10);

    }

// org.apache.commons.math3.geometry.euclidean.twod.LineTest::testIntersection
    public void testIntersection() {
        Line    l1 = new Line(new Vector2D( 0, 1), new Vector2D(1, 2));
        Line    l2 = new Line(new Vector2D(-1, 2), new Vector2D(2, 1));
        Vector2D p  = l1.intersection(l2);
        Assert.assertEquals(0.5, p.getX(), 1.0e-10);
        Assert.assertEquals(1.5, p.getY(), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testSimplyConnected
    public void testSimplyConnected() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(36.0, 22.0),
                new Vector2D(39.0, 32.0),
                new Vector2D(19.0, 32.0),
                new Vector2D( 6.0, 16.0),
                new Vector2D(31.0, 10.0),
                new Vector2D(42.0, 16.0),
                new Vector2D(34.0, 20.0),
                new Vector2D(29.0, 19.0),
                new Vector2D(23.0, 22.0),
                new Vector2D(33.0, 25.0)
            }
        };
        PolygonsSet set = buildSet(vertices);
        Assert.assertEquals(Region.Location.OUTSIDE, set.checkPoint(new Vector2D(50.0, 30.0)));
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(30.0, 15.0),
            new Vector2D(15.0, 20.0),
            new Vector2D(24.0, 25.0),
            new Vector2D(35.0, 30.0),
            new Vector2D(19.0, 17.0)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(50.0, 30.0),
            new Vector2D(30.0, 35.0),
            new Vector2D(10.0, 25.0),
            new Vector2D(10.0, 10.0),
            new Vector2D(40.0, 10.0),
            new Vector2D(50.0, 15.0),
            new Vector2D(30.0, 22.0)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(30.0, 32.0),
            new Vector2D(34.0, 20.0)
        });
        checkVertices(set.getVertices(), vertices);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testStair
    public void testStair() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0, 0.0),
                new Vector2D( 0.0, 2.0),
                new Vector2D(-0.1, 2.0),
                new Vector2D(-0.1, 1.0),
                new Vector2D(-0.3, 1.0),
                new Vector2D(-0.3, 1.5),
                new Vector2D(-1.3, 1.5),
                new Vector2D(-1.3, 2.0),
                new Vector2D(-1.8, 2.0),
                new Vector2D(-1.8 - 1.0 / FastMath.sqrt(2.0),
                            2.0 - 1.0 / FastMath.sqrt(2.0))
            }
        };

        PolygonsSet set = buildSet(vertices);
        checkVertices(set.getVertices(), vertices);

        Assert.assertEquals(1.1 + 0.95 * FastMath.sqrt(2.0), set.getSize(), 1.0e-10);

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testHole
    public void testHole() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(0.0, 0.0),
                new Vector2D(3.0, 0.0),
                new Vector2D(3.0, 3.0),
                new Vector2D(0.0, 3.0)
            }, new Vector2D[] {
                new Vector2D(1.0, 2.0),
                new Vector2D(2.0, 2.0),
                new Vector2D(2.0, 1.0),
                new Vector2D(1.0, 1.0)
            }
        };
        PolygonsSet set = buildSet(vertices);
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(0.5, 0.5),
            new Vector2D(1.5, 0.5),
            new Vector2D(2.5, 0.5),
            new Vector2D(0.5, 1.5),
            new Vector2D(2.5, 1.5),
            new Vector2D(0.5, 2.5),
            new Vector2D(1.5, 2.5),
            new Vector2D(2.5, 2.5),
            new Vector2D(0.5, 1.0)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(1.5, 1.5),
            new Vector2D(3.5, 1.0),
            new Vector2D(4.0, 1.5),
            new Vector2D(6.0, 6.0)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(1.5, 0.0),
            new Vector2D(1.5, 1.0),
            new Vector2D(1.5, 2.0),
            new Vector2D(1.5, 3.0),
            new Vector2D(3.0, 3.0)
        });
        checkVertices(set.getVertices(), vertices);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testDisjointPolygons
    public void testDisjointPolygons() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(0.0, 1.0),
                new Vector2D(2.0, 1.0),
                new Vector2D(1.0, 2.0)
            }, new Vector2D[] {
                new Vector2D(4.0, 0.0),
                new Vector2D(5.0, 1.0),
                new Vector2D(3.0, 1.0)
            }
        };
        PolygonsSet set = buildSet(vertices);
        Assert.assertEquals(Region.Location.INSIDE, set.checkPoint(new Vector2D(1.0, 1.5)));
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(1.0, 1.5),
            new Vector2D(4.5, 0.8)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(1.0, 0.0),
            new Vector2D(3.5, 1.2),
            new Vector2D(2.5, 1.0),
            new Vector2D(3.0, 4.0)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(3.5, 0.5),
            new Vector2D(0.0, 1.0)
        });
        checkVertices(set.getVertices(), vertices);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testOppositeHyperplanes
    public void testOppositeHyperplanes() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(1.0, 0.0),
                new Vector2D(2.0, 1.0),
                new Vector2D(3.0, 1.0),
                new Vector2D(2.0, 2.0),
                new Vector2D(1.0, 1.0),
                new Vector2D(0.0, 1.0)
            }
        };
        PolygonsSet set = buildSet(vertices);
        checkVertices(set.getVertices(), vertices);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testSingularPoint
    public void testSingularPoint() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 1.0,  0.0),
                new Vector2D( 1.0,  1.0),
                new Vector2D( 0.0,  1.0),
                new Vector2D( 0.0,  0.0),
                new Vector2D(-1.0,  0.0),
                new Vector2D(-1.0, -1.0),
                new Vector2D( 0.0, -1.0)
            }
        };
        PolygonsSet set = buildSet(vertices);
        checkVertices(set.getVertices(), vertices);
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testLineIntersection
    public void testLineIntersection() {
        Vector2D[][] vertices = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0),
                new Vector2D( 1.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        };
        PolygonsSet set = buildSet(vertices);

        Line l1 = new Line(new Vector2D(-1.5, 0.0), FastMath.PI / 4);
        SubLine s1 = (SubLine) set.intersection(l1.wholeHyperplane());
        List<Interval> i1 = ((IntervalsSet) s1.getRemainingRegion()).asList();
        Assert.assertEquals(2, i1.size());
        Interval v10 = i1.get(0);
        Vector2D p10Lower = l1.toSpace(new Vector1D(v10.getInf()));
        Assert.assertEquals(0.0, p10Lower.getX(), 1.0e-10);
        Assert.assertEquals(1.5, p10Lower.getY(), 1.0e-10);
        Vector2D p10Upper = l1.toSpace(new Vector1D(v10.getSup()));
        Assert.assertEquals(0.5, p10Upper.getX(), 1.0e-10);
        Assert.assertEquals(2.0, p10Upper.getY(), 1.0e-10);
        Interval v11 = i1.get(1);
        Vector2D p11Lower = l1.toSpace(new Vector1D(v11.getInf()));
        Assert.assertEquals(1.0, p11Lower.getX(), 1.0e-10);
        Assert.assertEquals(2.5, p11Lower.getY(), 1.0e-10);
        Vector2D p11Upper = l1.toSpace(new Vector1D(v11.getSup()));
        Assert.assertEquals(1.5, p11Upper.getX(), 1.0e-10);
        Assert.assertEquals(3.0, p11Upper.getY(), 1.0e-10);

        Line l2 = new Line(new Vector2D(-1.0, 2.0), 0);
        SubLine s2 = (SubLine) set.intersection(l2.wholeHyperplane());
        List<Interval> i2 = ((IntervalsSet) s2.getRemainingRegion()).asList();
        Assert.assertEquals(1, i2.size());
        Interval v20 = i2.get(0);
        Vector2D p20Lower = l2.toSpace(new Vector1D(v20.getInf()));
        Assert.assertEquals(1.0, p20Lower.getX(), 1.0e-10);
        Assert.assertEquals(2.0, p20Lower.getY(), 1.0e-10);
        Vector2D p20Upper = l2.toSpace(new Vector1D(v20.getSup()));
        Assert.assertEquals(3.0, p20Upper.getX(), 1.0e-10);
        Assert.assertEquals(2.0, p20Upper.getY(), 1.0e-10);

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testUnlimitedSubHyperplane
    public void testUnlimitedSubHyperplane() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(0.0, 0.0),
                new Vector2D(4.0, 0.0),
                new Vector2D(1.4, 1.5),
                new Vector2D(0.0, 3.5)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(1.4,  0.2),
                new Vector2D(2.8, -1.2),
                new Vector2D(2.5,  0.6)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);

        PolygonsSet set =
            (PolygonsSet) new RegionFactory<Euclidean2D>().union(set1.copySelf(),
                                                                 set2.copySelf());
        checkVertices(set1.getVertices(), vertices1);
        checkVertices(set2.getVertices(), vertices2);
        checkVertices(set.getVertices(), new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(0.0,  0.0),
                new Vector2D(1.6,  0.0),
                new Vector2D(2.8, -1.2),
                new Vector2D(2.6,  0.0),
                new Vector2D(4.0,  0.0),
                new Vector2D(1.4,  1.5),
                new Vector2D(0.0,  3.5)
            }
        });

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testUnion
    public void testUnion() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Euclidean2D>().union(set1.copySelf(),
                                                                                set2.copySelf());
        checkVertices(set1.getVertices(), vertices1);
        checkVertices(set2.getVertices(), vertices2);
        checkVertices(set.getVertices(), new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0),
                new Vector2D( 1.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        });
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(0.5, 0.5),
            new Vector2D(2.0, 2.0),
            new Vector2D(2.5, 2.5),
            new Vector2D(0.5, 1.5),
            new Vector2D(1.5, 1.5),
            new Vector2D(1.5, 0.5),
            new Vector2D(1.5, 2.5),
            new Vector2D(2.5, 1.5),
            new Vector2D(2.5, 2.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(-0.5, 0.5),
            new Vector2D( 0.5, 2.5),
            new Vector2D( 2.5, 0.5),
            new Vector2D( 3.5, 2.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(0.0, 0.0),
            new Vector2D(0.5, 2.0),
            new Vector2D(2.0, 0.5),
            new Vector2D(2.5, 1.0),
            new Vector2D(3.0, 2.5)
        });

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testIntersection
    public void testIntersection() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Euclidean2D>().intersection(set1.copySelf(),
                                                                                       set2.copySelf());
        checkVertices(set1.getVertices(), vertices1);
        checkVertices(set2.getVertices(), vertices2);
        checkVertices(set.getVertices(), new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 2.0,  1.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 1.0,  2.0)
            }
        });
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(1.5, 1.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(0.5, 1.5),
            new Vector2D(2.5, 1.5),
            new Vector2D(1.5, 0.5),
            new Vector2D(0.5, 0.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(2.0, 2.0),
            new Vector2D(1.0, 1.5),
            new Vector2D(1.5, 2.0)
        });
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testXor
    public void testXor() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Euclidean2D>().xor(set1.copySelf(),
                                                                              set2.copySelf());
        checkVertices(set1.getVertices(), vertices1);
        checkVertices(set2.getVertices(), vertices2);
        checkVertices(set.getVertices(), new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0),
                new Vector2D( 1.0,  2.0),
                new Vector2D( 0.0,  2.0)
            },
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 1.0,  2.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 2.0,  1.0)
            }
        });
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(0.5, 0.5),
            new Vector2D(2.5, 2.5),
            new Vector2D(0.5, 1.5),
            new Vector2D(1.5, 0.5),
            new Vector2D(1.5, 2.5),
            new Vector2D(2.5, 1.5),
            new Vector2D(2.5, 2.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D(-0.5, 0.5),
            new Vector2D( 0.5, 2.5),
            new Vector2D( 2.5, 0.5),
            new Vector2D( 1.5, 1.5),
            new Vector2D( 3.5, 2.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(2.0, 2.0),
            new Vector2D(1.5, 1.0),
            new Vector2D(2.0, 1.5),
            new Vector2D(0.0, 0.0),
            new Vector2D(0.5, 2.0),
            new Vector2D(2.0, 0.5),
            new Vector2D(2.5, 1.0),
            new Vector2D(3.0, 2.5)
        });
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testDifference
    public void testDifference() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0,  1.0),
                new Vector2D( 3.0,  1.0),
                new Vector2D( 3.0,  3.0),
                new Vector2D( 1.0,  3.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        PolygonsSet set  = (PolygonsSet) new RegionFactory<Euclidean2D>().difference(set1.copySelf(),
                                                                                     set2.copySelf());
        checkVertices(set1.getVertices(), vertices1);
        checkVertices(set2.getVertices(), vertices2);
        checkVertices(set.getVertices(), new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.0,  0.0),
                new Vector2D( 2.0,  0.0),
                new Vector2D( 2.0,  1.0),
                new Vector2D( 1.0,  1.0),
                new Vector2D( 1.0,  2.0),
                new Vector2D( 0.0,  2.0)
            }
        });
        checkPoints(Region.Location.INSIDE, set, new Vector2D[] {
            new Vector2D(0.5, 0.5),
            new Vector2D(0.5, 1.5),
            new Vector2D(1.5, 0.5)
        });
        checkPoints(Region.Location.OUTSIDE, set, new Vector2D[] {
            new Vector2D( 2.5, 2.5),
            new Vector2D(-0.5, 0.5),
            new Vector2D( 0.5, 2.5),
            new Vector2D( 2.5, 0.5),
            new Vector2D( 1.5, 1.5),
            new Vector2D( 3.5, 2.5),
            new Vector2D( 1.5, 2.5),
            new Vector2D( 2.5, 1.5),
            new Vector2D( 2.0, 1.5),
            new Vector2D( 2.0, 2.0),
            new Vector2D( 2.5, 1.0),
            new Vector2D( 2.5, 2.5),
            new Vector2D( 3.0, 2.5)
        });
        checkPoints(Region.Location.BOUNDARY, set, new Vector2D[] {
            new Vector2D(1.0, 1.0),
            new Vector2D(1.5, 1.0),
            new Vector2D(0.0, 0.0),
            new Vector2D(0.5, 2.0),
            new Vector2D(2.0, 0.5)
        });
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testEmptyDifference
    public void testEmptyDifference() {
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.5, 3.5),
                new Vector2D( 0.5, 4.5),
                new Vector2D(-0.5, 4.5),
                new Vector2D(-0.5, 3.5)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 1.0, 2.0),
                new Vector2D( 1.0, 8.0),
                new Vector2D(-1.0, 8.0),
                new Vector2D(-1.0, 2.0)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        Assert.assertTrue(new RegionFactory<Euclidean2D>().difference(set1.copySelf(), set2.copySelf()).isEmpty());
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testChoppedHexagon
    public void testChoppedHexagon() {
        double pi6   = FastMath.PI / 6.0;
        double sqrt3 = FastMath.sqrt(3.0);
        SubLine[] hyp = {
            new Line(new Vector2D(   0.0, 1.0),  5 * pi6).wholeHyperplane(),
            new Line(new Vector2D(-sqrt3, 1.0),  7 * pi6).wholeHyperplane(),
            new Line(new Vector2D(-sqrt3, 1.0),  9 * pi6).wholeHyperplane(),
            new Line(new Vector2D(-sqrt3, 0.0), 11 * pi6).wholeHyperplane(),
            new Line(new Vector2D(   0.0, 0.0), 13 * pi6).wholeHyperplane(),
            new Line(new Vector2D(   0.0, 1.0),  3 * pi6).wholeHyperplane(),
            new Line(new Vector2D(-5.0 * sqrt3 / 6.0, 0.0), 9 * pi6).wholeHyperplane()
        };
        hyp[1] = (SubLine) hyp[1].split(hyp[0].getHyperplane()).getMinus();
        hyp[2] = (SubLine) hyp[2].split(hyp[1].getHyperplane()).getMinus();
        hyp[3] = (SubLine) hyp[3].split(hyp[2].getHyperplane()).getMinus();
        hyp[4] = (SubLine) hyp[4].split(hyp[3].getHyperplane()).getMinus().split(hyp[0].getHyperplane()).getMinus();
        hyp[5] = (SubLine) hyp[5].split(hyp[4].getHyperplane()).getMinus().split(hyp[0].getHyperplane()).getMinus();
        hyp[6] = (SubLine) hyp[6].split(hyp[3].getHyperplane()).getMinus().split(hyp[1].getHyperplane()).getMinus();
        BSPTree<Euclidean2D> tree = new BSPTree<Euclidean2D>(Boolean.TRUE);
        for (int i = hyp.length - 1; i >= 0; --i) {
            tree = new BSPTree<Euclidean2D>(hyp[i], new BSPTree<Euclidean2D>(Boolean.FALSE), tree, null);
        }
        PolygonsSet set = new PolygonsSet(tree);
        SubLine splitter =
            new Line(new Vector2D(-2.0 * sqrt3 / 3.0, 0.0), 9 * pi6).wholeHyperplane();
        PolygonsSet slice =
            new PolygonsSet(new BSPTree<Euclidean2D>(splitter,
                                                     set.getTree(false).split(splitter).getPlus(),
                                                     new BSPTree<Euclidean2D>(Boolean.FALSE), null));
        Assert.assertEquals(Region.Location.OUTSIDE,
                            slice.checkPoint(new Vector2D(0.1, 0.5)));
        Assert.assertEquals(11.0 / 3.0, slice.getBoundarySize(), 1.0e-10);

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testConcentric
    public void testConcentric() {
        double h = FastMath.sqrt(3.0) / 2.0;
        Vector2D[][] vertices1 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.00, 0.1 * h),
                new Vector2D( 0.05, 0.1 * h),
                new Vector2D( 0.10, 0.2 * h),
                new Vector2D( 0.05, 0.3 * h),
                new Vector2D(-0.05, 0.3 * h),
                new Vector2D(-0.10, 0.2 * h),
                new Vector2D(-0.05, 0.1 * h)
            }
        };
        PolygonsSet set1 = buildSet(vertices1);
        Vector2D[][] vertices2 = new Vector2D[][] {
            new Vector2D[] {
                new Vector2D( 0.00, 0.0 * h),
                new Vector2D( 0.10, 0.0 * h),
                new Vector2D( 0.20, 0.2 * h),
                new Vector2D( 0.10, 0.4 * h),
                new Vector2D(-0.10, 0.4 * h),
                new Vector2D(-0.20, 0.2 * h),
                new Vector2D(-0.10, 0.0 * h)
            }
        };
        PolygonsSet set2 = buildSet(vertices2);
        Assert.assertTrue(set2.contains(set1));
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testBug20040520
    public void testBug20040520() {
        BSPTree<Euclidean2D> a0 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.85, -0.05),
                                                  new Vector2D(0.90, -0.10)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                  new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Euclidean2D> a1 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.85, -0.10),
                                                  new Vector2D(0.90, -0.10)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE), a0, null);
        BSPTree<Euclidean2D> a2 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.90, -0.05),
                                                  new Vector2D(0.85, -0.05)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE), a1, null);
        BSPTree<Euclidean2D> a3 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.82, -0.05),
                                                  new Vector2D(0.82, -0.08)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                  new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Euclidean2D> a4 =
            new BSPTree<Euclidean2D>(buildHalfLine(new Vector2D(0.85, -0.05),
                                                   new Vector2D(0.80, -0.05),
                                                   false),
                                                   new BSPTree<Euclidean2D>(Boolean.FALSE), a3, null);
        BSPTree<Euclidean2D> a5 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.82, -0.08),
                                                  new Vector2D(0.82, -0.18)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                  new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Euclidean2D> a6 =
            new BSPTree<Euclidean2D>(buildHalfLine(new Vector2D(0.82, -0.18),
                                                   new Vector2D(0.85, -0.15),
                                                   true),
                                                   new BSPTree<Euclidean2D>(Boolean.FALSE), a5, null);
        BSPTree<Euclidean2D> a7 =
            new BSPTree<Euclidean2D>(buildHalfLine(new Vector2D(0.85, -0.05),
                                                   new Vector2D(0.82, -0.08),
                                                   false),
                                                   a4, a6, null);
        BSPTree<Euclidean2D> a8 =
            new BSPTree<Euclidean2D>(buildLine(new Vector2D(0.85, -0.25),
                                               new Vector2D(0.85,  0.05)),
                                               a2, a7, null);
        BSPTree<Euclidean2D> a9 =
            new BSPTree<Euclidean2D>(buildLine(new Vector2D(0.90,  0.05),
                                               new Vector2D(0.90, -0.50)),
                                               a8, new BSPTree<Euclidean2D>(Boolean.FALSE), null);

        BSPTree<Euclidean2D> b0 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.92, -0.12),
                                                  new Vector2D(0.92, -0.08)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE), new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Euclidean2D> b1 =
            new BSPTree<Euclidean2D>(buildHalfLine(new Vector2D(0.92, -0.08),
                                                   new Vector2D(0.90, -0.10),
                                                   true),
                                                   new BSPTree<Euclidean2D>(Boolean.FALSE), b0, null);
        BSPTree<Euclidean2D> b2 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.92, -0.18),
                                                  new Vector2D(0.92, -0.12)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE), new BSPTree<Euclidean2D>(Boolean.TRUE),
                                                  null);
        BSPTree<Euclidean2D> b3 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.85, -0.15),
                                                  new Vector2D(0.90, -0.20)),
                                                  new BSPTree<Euclidean2D>(Boolean.FALSE), b2, null);
        BSPTree<Euclidean2D> b4 =
            new BSPTree<Euclidean2D>(buildSegment(new Vector2D(0.95, -0.15),
                                                  new Vector2D(0.85, -0.05)),
                                                  b1, b3, null);
        BSPTree<Euclidean2D> b5 =
            new BSPTree<Euclidean2D>(buildHalfLine(new Vector2D(0.85, -0.05),
                                                   new Vector2D(0.85, -0.25),
                                                   true),
                                                   new BSPTree<Euclidean2D>(Boolean.FALSE), b4, null);
        BSPTree<Euclidean2D> b6 =
            new BSPTree<Euclidean2D>(buildLine(new Vector2D(0.0, -1.10),
                                               new Vector2D(1.0, -0.10)),
                                               new BSPTree<Euclidean2D>(Boolean.FALSE), b5, null);

        PolygonsSet c =
            (PolygonsSet) new RegionFactory<Euclidean2D>().union(new PolygonsSet(a9),
                                                                 new PolygonsSet(b6));

        checkPoints(Region.Location.INSIDE, c, new Vector2D[] {
            new Vector2D(0.83, -0.06),
            new Vector2D(0.83, -0.15),
            new Vector2D(0.88, -0.15),
            new Vector2D(0.88, -0.09),
            new Vector2D(0.88, -0.07),
            new Vector2D(0.91, -0.18),
            new Vector2D(0.91, -0.10)
        });

        checkPoints(Region.Location.OUTSIDE, c, new Vector2D[] {
            new Vector2D(0.80, -0.10),
            new Vector2D(0.83, -0.50),
            new Vector2D(0.83, -0.20),
            new Vector2D(0.83, -0.02),
            new Vector2D(0.87, -0.50),
            new Vector2D(0.87, -0.20),
            new Vector2D(0.87, -0.02),
            new Vector2D(0.91, -0.20),
            new Vector2D(0.91, -0.08),
            new Vector2D(0.93, -0.15)
        });

        checkVertices(c.getVertices(),
                      new Vector2D[][] {
            new Vector2D[] {
                new Vector2D(0.85, -0.15),
                new Vector2D(0.90, -0.20),
                new Vector2D(0.92, -0.18),
                new Vector2D(0.92, -0.08),
                new Vector2D(0.90, -0.10),
                new Vector2D(0.90, -0.05),
                new Vector2D(0.82, -0.05),
                new Vector2D(0.82, -0.18),
            }
        });

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testBug20041003
    public void testBug20041003() {

        Line[] l = {
            new Line(new Vector2D(0.0, 0.625000007541172),
                     new Vector2D(1.0, 0.625000007541172)),
                     new Line(new Vector2D(-0.19204433621902645, 0.0),
                              new Vector2D(-0.19204433621902645, 1.0)),
                              new Line(new Vector2D(-0.40303524786887,  0.4248364535319128),
                                       new Vector2D(-1.12851149797877, -0.2634107480798909)),
                                       new Line(new Vector2D(0.0, 2.0),
                                                new Vector2D(1.0, 2.0))
        };

        BSPTree<Euclidean2D> node1 =
            new BSPTree<Euclidean2D>(new SubLine(l[0],
                                          new IntervalsSet(intersectionAbscissa(l[0], l[1]),
                                                           intersectionAbscissa(l[0], l[2]))),
                                                           new BSPTree<Euclidean2D>(Boolean.TRUE), new BSPTree<Euclidean2D>(Boolean.FALSE),
                                                           null);
        BSPTree<Euclidean2D> node2 =
            new BSPTree<Euclidean2D>(new SubLine(l[1],
                                          new IntervalsSet(intersectionAbscissa(l[1], l[2]),
                                                           intersectionAbscissa(l[1], l[3]))),
                                                           node1, new BSPTree<Euclidean2D>(Boolean.FALSE), null);
        BSPTree<Euclidean2D> node3 =
            new BSPTree<Euclidean2D>(new SubLine(l[2],
                                          new IntervalsSet(intersectionAbscissa(l[2], l[3]),
                                                           Double.POSITIVE_INFINITY)),
                                                           node2, new BSPTree<Euclidean2D>(Boolean.FALSE), null);
        BSPTree<Euclidean2D> node4 =
            new BSPTree<Euclidean2D>(l[3].wholeHyperplane(), node3, new BSPTree<Euclidean2D>(Boolean.FALSE), null);

        PolygonsSet set = new PolygonsSet(node4);
        Assert.assertEquals(0, set.getVertices().length);

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testSqueezedHexa
    public void testSqueezedHexa() {
        PolygonsSet set = new PolygonsSet(1.0e-10,
                                          new Vector2D(-6, -4), new Vector2D(-8, -8), new Vector2D(  8, -8),
                                          new Vector2D( 6, -4), new Vector2D(10,  4), new Vector2D(-10,  4));
        Assert.assertEquals(Location.OUTSIDE, set.checkPoint(new Vector2D(0, 6)));
    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testIssue880Simplified
    public void testIssue880Simplified() {

        Vector2D[] vertices1 = new Vector2D[] {
            new Vector2D( 90.13595870833188,  38.33604606376991),
            new Vector2D( 90.14047850603913,  38.34600084496253),
            new Vector2D( 90.11045289492762,  38.36801537312368),
            new Vector2D( 90.10871471476526,  38.36878044144294),
            new Vector2D( 90.10424901707671,  38.374300101757),
            new Vector2D( 90.0979455456843,   38.373578376172475),
            new Vector2D( 90.09081227075944,  38.37526295920463),
            new Vector2D( 90.09081378927135,  38.375193883266434)
        };
        PolygonsSet set1 = new PolygonsSet(1.0e-10, vertices1);
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(new Vector2D(90.12,  38.32)));
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(new Vector2D(90.135, 38.355)));

    }

// org.apache.commons.math3.geometry.euclidean.twod.PolygonsSetTest::testIssue880Complete
    public void testIssue880Complete() {
        Vector2D[] vertices1 = new Vector2D[] {
                new Vector2D( 90.08714908223715,  38.370299337260235),
                new Vector2D( 90.08709517675004,  38.3702895991413),
                new Vector2D( 90.08401538704919,  38.368849330127944),
                new Vector2D( 90.08258210430711,  38.367634558585564),
                new Vector2D( 90.08251455106665,  38.36763409247078),
                new Vector2D( 90.08106599752608,  38.36761621664249),
                new Vector2D( 90.08249585300035,  38.36753627557965),
                new Vector2D( 90.09075743352184,  38.35914647644972),
                new Vector2D( 90.09099945896571,  38.35896264724079),
                new Vector2D( 90.09269383800086,  38.34595756121246),
                new Vector2D( 90.09638631543191,  38.3457988093121),
                new Vector2D( 90.09666417351019,  38.34523360999418),
                new Vector2D( 90.1297082145872,  38.337670454923625),
                new Vector2D( 90.12971687748956,  38.337669827794684),
                new Vector2D( 90.1240820219179,  38.34328502001131),
                new Vector2D( 90.13084259656404,  38.34017811765017),
                new Vector2D( 90.13378567942857,  38.33860579180606),
                new Vector2D( 90.13519557833206,  38.33621054663689),
                new Vector2D( 90.13545616732307,  38.33614965452864),
                new Vector2D( 90.13553111202748,  38.33613962818305),
                new Vector2D( 90.1356903436448,  38.33610227127048),
                new Vector2D( 90.13576283227428,  38.33609255422783),
                new Vector2D( 90.13595870833188,  38.33604606376991),
                new Vector2D( 90.1361556630693,  38.3360024198866),
                new Vector2D( 90.13622408795709,  38.335987048115726),
                new Vector2D( 90.13696189099994,  38.33581914328681),
                new Vector2D( 90.13746655304897,  38.33616706665265),
                new Vector2D( 90.13845973716064,  38.33650776167099),
                new Vector2D( 90.13950901827667,  38.3368469456463),
                new Vector2D( 90.14393814424852,  38.337591835857495),
                new Vector2D( 90.14483839716831,  38.337076122362475),
                new Vector2D( 90.14565474433601,  38.33769000964429),
                new Vector2D( 90.14569421179482,  38.3377117256905),
                new Vector2D( 90.14577067124333,  38.33770883625908),
                new Vector2D( 90.14600350631684,  38.337714326520995),
                new Vector2D( 90.14600355139731,  38.33771435193319),
                new Vector2D( 90.14600369112401,  38.33771443882085),
                new Vector2D( 90.14600382486884,  38.33771453466096),
                new Vector2D( 90.14600395205912,  38.33771463904344),
                new Vector2D( 90.14600407214999,  38.337714751520764),
                new Vector2D( 90.14600418462749,  38.337714871611695),
                new Vector2D( 90.14600422249327,  38.337714915811034),
                new Vector2D( 90.14867838361471,  38.34113888210675),
                new Vector2D( 90.14923750157374,  38.341582537502575),
                new Vector2D( 90.14877083250991,  38.34160685841391),
                new Vector2D( 90.14816667319519,  38.34244232585684),
                new Vector2D( 90.14797696744586,  38.34248455284745),
                new Vector2D( 90.14484318014337,  38.34385573215269),
                new Vector2D( 90.14477919958296,  38.3453797747614),
                new Vector2D( 90.14202393306448,  38.34464324839456),
                new Vector2D( 90.14198920640195,  38.344651155237216),
                new Vector2D( 90.14155207025175,  38.34486424263724),
                new Vector2D( 90.1415196143314,  38.344871730519),
                new Vector2D( 90.14128611910814,  38.34500196593859),
                new Vector2D( 90.14047850603913,  38.34600084496253),
                new Vector2D( 90.14045907000337,  38.34601860032171),
                new Vector2D( 90.14039496493928,  38.346223030432384),
                new Vector2D( 90.14037626063737,  38.346240203360026),
                new Vector2D( 90.14030005823724,  38.34646920000705),
                new Vector2D( 90.13799164754806,  38.34903093011013),
                new Vector2D( 90.11045289492762,  38.36801537312368),
                new Vector2D( 90.10871471476526,  38.36878044144294),
                new Vector2D( 90.10424901707671,  38.374300101757),
                new Vector2D( 90.10263482039932,  38.37310041316073),
                new Vector2D( 90.09834601753448,  38.373615053823414),
                new Vector2D( 90.0979455456843,  38.373578376172475),
                new Vector2D( 90.09086514328669,  38.37527884194668),
                new Vector2D( 90.09084931407364,  38.37590801712463),
                new Vector2D( 90.09081227075944,  38.37526295920463),
                new Vector2D( 90.09081378927135,  38.375193883266434)
        };
        PolygonsSet set1 = new PolygonsSet(1.0e-8, vertices1);
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(new Vector2D(90.0905,  38.3755)));
        Assert.assertEquals(Location.INSIDE,  set1.checkPoint(new Vector2D(90.09084, 38.3755)));
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(new Vector2D(90.0913,  38.3755)));
        Assert.assertEquals(Location.INSIDE,  set1.checkPoint(new Vector2D(90.1042,  38.3739)));
        Assert.assertEquals(Location.INSIDE,  set1.checkPoint(new Vector2D(90.1111,  38.3673)));
        Assert.assertEquals(Location.OUTSIDE, set1.checkPoint(new Vector2D(90.0959,  38.3457)));

        Vector2D[] vertices2 = new Vector2D[] {
                new Vector2D( 90.13067558880044,  38.36977255037573),
                new Vector2D( 90.12907570488,  38.36817308242706),
                new Vector2D( 90.1342774136516,  38.356886880294724),
                new Vector2D( 90.13090330629757,  38.34664392676211),
                new Vector2D( 90.13078571364593,  38.344904617518466),
                new Vector2D( 90.1315602208914,  38.3447185040846),
                new Vector2D( 90.1316336226821,  38.34470643148342),
                new Vector2D( 90.134020944832,  38.340936644972885),
                new Vector2D( 90.13912536387306,  38.335497255122334),
                new Vector2D( 90.1396178806582,  38.334878075552126),
                new Vector2D( 90.14083049696671,  38.33316530644106),
                new Vector2D( 90.14145252901329,  38.33152722916191),
                new Vector2D( 90.1404779335565,  38.32863516047786),
                new Vector2D( 90.14282712131586,  38.327504432532066),
                new Vector2D( 90.14616669875488,  38.3237354115015),
                new Vector2D( 90.14860976050608,  38.315714862457924),
                new Vector2D( 90.14999277782437,  38.3164932507504),
                new Vector2D( 90.15005207194997,  38.316534677663356),
                new Vector2D( 90.15508513859612,  38.31878731691609),
                new Vector2D( 90.15919938519221,  38.31852743183782),
                new Vector2D( 90.16093758658837,  38.31880662005153),
                new Vector2D( 90.16099420184912,  38.318825953291594),
                new Vector2D( 90.1665411125756,  38.31859497874757),
                new Vector2D( 90.16999653861313,  38.32505772048029),
                new Vector2D( 90.17475243391698,  38.32594398441148),
                new Vector2D( 90.17940844844992,  38.327427213761325),
                new Vector2D( 90.20951909541378,  38.330616833491774),
                new Vector2D( 90.2155400467941,  38.331746223670336),
                new Vector2D( 90.21559881391778,  38.33175551425302),
                new Vector2D( 90.21916646426041,  38.332584299620805),
                new Vector2D( 90.23863749852285,  38.34778978875795),
                new Vector2D( 90.25459855175802,  38.357790570608984),
                new Vector2D( 90.25964298227257,  38.356918010203174),
                new Vector2D( 90.26024593994703,  38.361692743151366),
                new Vector2D( 90.26146187570015,  38.36311080550837),
                new Vector2D( 90.26614159359622,  38.36510808579902),
                new Vector2D( 90.26621342936448,  38.36507942500333),
                new Vector2D( 90.26652190211962,  38.36494042196722),
                new Vector2D( 90.26621240678867,  38.365113172030874),
                new Vector2D( 90.26614057102057,  38.365141832826794),
                new Vector2D( 90.26380080055299,  38.3660381760273),
                new Vector2D( 90.26315345241,  38.36670658276421),
                new Vector2D( 90.26251574942881,  38.367490323488084),
                new Vector2D( 90.26247873448426,  38.36755266444749),
                new Vector2D( 90.26234628016698,  38.36787989125406),
                new Vector2D( 90.26214559424784,  38.36945909356126),
                new Vector2D( 90.25861728442555,  38.37200753430875),
                new Vector2D( 90.23905557537864,  38.375405314295904),
                new Vector2D( 90.22517251874075,  38.38984691662256),
                new Vector2D( 90.22549955153215,  38.3911564273979),
                new Vector2D( 90.22434386063355,  38.391476432092134),
                new Vector2D( 90.22147729457276,  38.39134652252034),
                new Vector2D( 90.22142070120117,  38.391349167741964),
                new Vector2D( 90.20665060751588,  38.39475580900313),
                new Vector2D( 90.20042268367109,  38.39842558622888),
                new Vector2D( 90.17423771242085,  38.402727751805344),
                new Vector2D( 90.16756796257476,  38.40913898597597),
                new Vector2D( 90.16728283954308,  38.411255399912875),
                new Vector2D( 90.16703538220418,  38.41136059866693),
                new Vector2D( 90.16725865657685,  38.41013618805954),
                new Vector2D( 90.16746107640665,  38.40902614307544),
                new Vector2D( 90.16122795307462,  38.39773101873203)
        };
        PolygonsSet set2 = new PolygonsSet(1.0e-8, vertices2);
        PolygonsSet set  = (PolygonsSet) new
                RegionFactory<Euclidean2D>().difference(set1.copySelf(),
                                                        set2.copySelf());

        Vector2D[][] verticies = set.getVertices();
        Assert.assertTrue(verticies[0][0] != null);
        Assert.assertEquals(1, verticies.length);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SegmentTest::testDistance
    public void testDistance() {
        Vector2D start = new Vector2D(2, 2);
        Vector2D end = new Vector2D(-2, -2);
        Segment segment = new Segment(start, end, new Line(start, end));

        
        Assert.assertEquals(FastMath.sqrt(2), segment.distance(new Vector2D(1, -1)), 1.0e-10);

        
        Assert.assertEquals(FastMath.sin(Math.PI / 4.0), segment.distance(new Vector2D(0, -1)), 1.0e-10);

        
        Assert.assertEquals(FastMath.sqrt(8), segment.distance(new Vector2D(0, 4)), 1.0e-10);

        
        Assert.assertEquals(FastMath.sqrt(8), segment.distance(new Vector2D(0, -4)), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testEndPoints
    public void testEndPoints() {
        Vector2D p1 = new Vector2D(-1, -7);
        Vector2D p2 = new Vector2D(7, -1);
        Segment segment = new Segment(p1, p2, new Line(p1, p2));
        SubLine sub = new SubLine(segment);
        List<Segment> segments = sub.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, new Vector2D(-1, -7).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertEquals(0.0, new Vector2D( 7, -1).distance(segments.get(0).getEnd()), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testNoEndPoints
    public void testNoEndPoints() {
        SubLine wholeLine = new Line(new Vector2D(-1, 7), new Vector2D(7, 1)).wholeHyperplane();
        List<Segment> segments = wholeLine.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() < 0);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testNoSegments
    public void testNoSegments() {
        SubLine empty = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new RegionFactory<Euclidean1D>().getComplement(new IntervalsSet()));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(0, segments.size());
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testSeveralSegments
    public void testSeveralSegments() {
        SubLine twoSubs = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new RegionFactory<Euclidean1D>().union(new IntervalsSet(1, 2),
                                                                           new IntervalsSet(3, 4)));
        List<Segment> segments = twoSubs.getSegments();
        Assert.assertEquals(2, segments.size());
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testHalfInfiniteNeg
    public void testHalfInfiniteNeg() {
        SubLine empty = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new IntervalsSet(Double.NEGATIVE_INFINITY, 0.0));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getX()) &&
                          segments.get(0).getStart().getX() < 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getStart().getY()) &&
                          segments.get(0).getStart().getY() < 0);
        Assert.assertEquals(0.0, new Vector2D(3, -4).distance(segments.get(0).getEnd()), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testHalfInfinitePos
    public void testHalfInfinitePos() {
        SubLine empty = new SubLine(new Line(new Vector2D(-1, -7), new Vector2D(7, -1)),
                                    new IntervalsSet(0.0, Double.POSITIVE_INFINITY));
        List<Segment> segments = empty.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertEquals(0.0, new Vector2D(3, -4).distance(segments.get(0).getStart()), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getX()) &&
                          segments.get(0).getEnd().getX() > 0);
        Assert.assertTrue(Double.isInfinite(segments.get(0).getEnd().getY()) &&
                          segments.get(0).getEnd().getY() > 0);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionInsideInside
    public void testIntersectionInsideInside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(3, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 2));
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, false)), 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionInsideBoundary
    public void testIntersectionInsideBoundary() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(3, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 1));
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionInsideOutside
    public void testIntersectionInsideOutside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(3, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionBoundaryBoundary
    public void testIntersectionBoundaryBoundary() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(2, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 1));
        Assert.assertEquals(0.0, new Vector2D(2, 1).distance(sub1.intersection(sub2, true)),  1.0e-12);
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionBoundaryOutside
    public void testIntersectionBoundaryOutside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(2, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.geometry.euclidean.twod.SubLineTest::testIntersectionOutsideOutside
    public void testIntersectionOutsideOutside() {
        SubLine sub1 = new SubLine(new Vector2D(1, 1), new Vector2D(1.5, 1));
        SubLine sub2 = new SubLine(new Vector2D(2, 0), new Vector2D(2, 0.5));
        Assert.assertNull(sub1.intersection(sub2, true));
        Assert.assertNull(sub1.intersection(sub2, false));
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testDimensions
    public void testDimensions() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        Assert.assertEquals("testData row dimension",3,m.getRowDimension());
        Assert.assertEquals("testData column dimension",3,m.getColumnDimension());
        Assert.assertTrue("testData is square",m.isSquare());
        Assert.assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        Assert.assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        Assert.assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Array2DRowRealMatrix m1 = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(m1.getData());
        Assert.assertEquals(m2,m1);
        Array2DRowRealMatrix m3 = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m4 = new Array2DRowRealMatrix(m3.getData(), false);
        Assert.assertEquals(m4,m3);
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testAdd
    public void testAdd() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        RealMatrix mPlusMInv = m.add(mInv);
        double[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals("sum entry entry",
                    testDataPlusInv[row][col],sumEntries[row][col],
                        entryTolerance);
            }
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testAddFail
    public void testAddFail() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testNorm
    public void testNorm() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        Assert.assertEquals("testData norm",14d,m.getNorm(),entryTolerance);
        Assert.assertEquals("testData2 norm",7d,m2.getNorm(),entryTolerance);
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testFrobeniusNorm
    public void testFrobeniusNorm() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testData2);
        Assert.assertEquals("testData Frobenius norm", FastMath.sqrt(117.0), m.getFrobeniusNorm(), entryTolerance);
        Assert.assertEquals("testData2 Frobenius norm", FastMath.sqrt(52.0), m2.getFrobeniusNorm(), entryTolerance);
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testPlusMinus
    public void testPlusMinus() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix(testDataInv);
        TestUtils.assertEquals("m-n = m + -n",m.subtract(m2),
            m2.scalarMultiply(-1d).add(m),entryTolerance);
        try {
            m.subtract(new Array2DRowRealMatrix(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testMultiply
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
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testMultiply2
    public void testMultiply2() {
       RealMatrix m3 = new Array2DRowRealMatrix(d3);
       RealMatrix m4 = new Array2DRowRealMatrix(d4);
       RealMatrix m5 = new Array2DRowRealMatrix(d5);
       TestUtils.assertEquals("m3*m4=m5", m3.multiply(m4), m5, entryTolerance);
   }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testPower
    public void testPower() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix mInv = new Array2DRowRealMatrix(testDataInv);
        Array2DRowRealMatrix mPlusInv = new Array2DRowRealMatrix(testDataPlusInv);
        Array2DRowRealMatrix identity = new Array2DRowRealMatrix(id);

        TestUtils.assertEquals("m^0", m.power(0),
            identity, entryTolerance);
        TestUtils.assertEquals("mInv^0", mInv.power(0),
                identity, entryTolerance);
        TestUtils.assertEquals("mPlusInv^0", mPlusInv.power(0),
                identity, entryTolerance);

        TestUtils.assertEquals("m^1", m.power(1),
                m, entryTolerance);
        TestUtils.assertEquals("mInv^1", mInv.power(1),
                mInv, entryTolerance);
        TestUtils.assertEquals("mPlusInv^1", mPlusInv.power(1),
                mPlusInv, entryTolerance);

        RealMatrix C1 = m.copy();
        RealMatrix C2 = mInv.copy();
        RealMatrix C3 = mPlusInv.copy();

        for (int i = 2; i <= 10; ++i) {
            C1 = C1.multiply(m);
            C2 = C2.multiply(mInv);
            C3 = C3.multiply(mPlusInv);

            TestUtils.assertEquals("m^" + i, m.power(i),
                    C1, entryTolerance);
            TestUtils.assertEquals("mInv^" + i, mInv.power(i),
                    C2, entryTolerance);
            TestUtils.assertEquals("mPlusInv^" + i, mPlusInv.power(i),
                    C3, entryTolerance);
        }

        try {
            Array2DRowRealMatrix mNotSquare = new Array2DRowRealMatrix(testData2T);
            mNotSquare.power(2);
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }

        try {
            m.power(-1);
            Assert.fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testTrace
    public void testTrace() {
        RealMatrix m = new Array2DRowRealMatrix(id);
        Assert.assertEquals("identity trace",3d,m.getTrace(),entryTolerance);
        m = new Array2DRowRealMatrix(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testScalarAdd
    public void testScalarAdd() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("scalar add",new Array2DRowRealMatrix(testDataPlus2),
            m.scalarAdd(2d),entryTolerance);
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testOperate
    public void testOperate() {
        RealMatrix m = new Array2DRowRealMatrix(id);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(testVector), entryTolerance);
        TestUtils.assertEquals("identity operate", testVector,
                    m.operate(new ArrayRealVector(testVector)).toArray(), entryTolerance);
        m = new Array2DRowRealMatrix(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testMath209
    public void testMath209() {
        RealMatrix a = new Array2DRowRealMatrix(new double[][] {
                { 1, 2 }, { 3, 4 }, { 5, 6 }
        }, false);
        double[] b = a.operate(new double[] { 1, 1 });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals( 3.0, b[0], 1.0e-12);
        Assert.assertEquals( 7.0, b[1], 1.0e-12);
        Assert.assertEquals(11.0, b[2], 1.0e-12);
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testTranspose
    public void testTranspose() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        RealMatrix mIT = new LUDecomposition(m).getSolver().getInverse().transpose();
        RealMatrix mTI = new LUDecomposition(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals("inverse-transpose", mIT, mTI, normTolerance);
        m = new Array2DRowRealMatrix(testData2);
        RealMatrix mt = new Array2DRowRealMatrix(testData2T);
        TestUtils.assertEquals("transpose",mt,m.transpose(),normTolerance);
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("premultiply", m.preMultiply(testVector),
                    preMultTest, normTolerance);
        TestUtils.assertEquals("premultiply", m.preMultiply(new ArrayRealVector(testVector).toArray()),
                    preMultTest, normTolerance);
        m = new Array2DRowRealMatrix(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testPremultiply
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
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testGetVectors
    public void testGetVectors() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        TestUtils.assertEquals("get row",m.getRow(0),testDataRow1,entryTolerance);
        TestUtils.assertEquals("get col",m.getColumn(2),testDataCol3,entryTolerance);
        try {
            m.getRow(10);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testGetEntry
    public void testGetEntry() {
        RealMatrix m = new Array2DRowRealMatrix(testData);
        Assert.assertEquals("get entry",m.getEntry(0,1),2d,entryTolerance);
        try {
            m.getEntry(10, 4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testExamples
    public void testExamples() {
        
        double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
        RealMatrix m = new Array2DRowRealMatrix(matrixData);
        
        double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
        RealMatrix n = new Array2DRowRealMatrix(matrixData2);
        
        RealMatrix p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        RealMatrix pInverse = new LUDecomposition(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        double[][] coefficientsData = {{2, 3, -2}, {-1, 7, 6}, {4, -3, -5}};
        RealMatrix coefficients = new Array2DRowRealMatrix(coefficientsData);
        RealVector constants = new ArrayRealVector(new double[]{1, -2, 1}, false);
        RealVector solution = new LUDecomposition(coefficients).getSolver().solve(constants);
        final double cst0 = constants.getEntry(0);
        final double cst1 = constants.getEntry(1);
        final double cst2 = constants.getEntry(2);
        final double sol0 = solution.getEntry(0);
        final double sol1 = solution.getEntry(1);
        final double sol2 = solution.getEntry(2);
        Assert.assertEquals(2 * sol0 + 3 * sol1 -2 * sol2, cst0, 1E-12);
        Assert.assertEquals(-1 * sol0 + 7 * sol1 + 6 * sol2, cst1, 1E-12);
        Assert.assertEquals(4 * sol0 - 3 * sol1 -5 * sol2, cst2, 1E-12);
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testGetSubMatrix
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

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testCopySubMatrix
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

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mRow0 = new Array2DRowRealMatrix(subRow0);
        RealMatrix mRow3 = new Array2DRowRealMatrix(subRow3);
        Assert.assertEquals("Row0", mRow0,
                m.getRowMatrix(0));
        Assert.assertEquals("Row3", mRow3,
                m.getRowMatrix(3));
        try {
            m.getRowMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mRow3 = new Array2DRowRealMatrix(subRow3);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowMatrix(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowMatrix(0));
        try {
            m.setRowMatrix(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mColumn1 = new Array2DRowRealMatrix(subColumn1);
        RealMatrix mColumn3 = new Array2DRowRealMatrix(subColumn3);
        Assert.assertEquals("Column1", mColumn1,
                m.getColumnMatrix(1));
        Assert.assertEquals("Column3", mColumn3,
                m.getColumnMatrix(3));
        try {
            m.getColumnMatrix(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnMatrix(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealMatrix mColumn3 = new Array2DRowRealMatrix(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnMatrix(1));
        m.setColumnMatrix(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnMatrix(1));
        try {
            m.setColumnMatrix(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnMatrix(0, m);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testGetRowVector
    public void testGetRowVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mRow0 = new ArrayRealVector(subRow0[0]);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        Assert.assertEquals("Row0", mRow0, m.getRowVector(0));
        Assert.assertEquals("Row3", mRow3, m.getRowVector(3));
        try {
            m.getRowVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRowVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testSetRowVector
    public void testSetRowVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mRow3 = new ArrayRealVector(subRow3[0]);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayRealVector(5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mColumn1 = columnToVector(subColumn1);
        RealVector mColumn3 = columnToVector(subColumn3);
        Assert.assertEquals("Column1", mColumn1, m.getColumnVector(1));
        Assert.assertEquals("Column3", mColumn3, m.getColumnVector(3));
        try {
            m.getColumnVector(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumnVector(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        RealVector mColumn3 = columnToVector(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayRealVector(5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testGetRow
    public void testGetRow() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        checkArrays(subRow0[0], m.getRow(0));
        checkArrays(subRow3[0], m.getRow(3));
        try {
            m.getRow(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getRow(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testSetRow
    public void testSetRow() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        Assert.assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new double[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testGetColumn
    public void testGetColumn() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        double[] mColumn1 = columnToArray(subColumn1);
        double[] mColumn3 = columnToArray(subColumn3);
        checkArrays(mColumn1, m.getColumn(1));
        checkArrays(mColumn3, m.getColumn(3));
        try {
            m.getColumn(-1);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.getColumn(4);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testSetColumn
    public void testSetColumn() {
        RealMatrix m = new Array2DRowRealMatrix(subTestData);
        double[] mColumn3 = columnToArray(subColumn3);
        Assert.assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new double[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Array2DRowRealMatrix m1 = (Array2DRowRealMatrix) m.copy();
        Array2DRowRealMatrix mt = (Array2DRowRealMatrix) m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(new Array2DRowRealMatrix(bigSingular)));
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testToString
    public void testToString() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Assert.assertEquals("Array2DRowRealMatrix{{1.0,2.0,3.0},{2.0,5.0,3.0},{1.0,0.0,8.0}}",
                m.toString());
        m = new Array2DRowRealMatrix();
        Assert.assertEquals("Array2DRowRealMatrix{}",
                m.toString());
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        m.setSubMatrix(detData2,1,1);
        RealMatrix expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,2.0,3.0},{2.0,1.0,3.0},{1.0,2.0,4.0}});
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{1.0,3.0,3.0},{2.0,4.0,3.0},{1.0,2.0,4.0}});
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = MatrixUtils.createRealMatrix
            (new double[][] {{3.0,4.0,5.0},{4.0,7.0,5.0},{3.0,2.0,10.0}});
        Assert.assertEquals(expected, m);

        
        try {
            m.setSubMatrix(testData,1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        
        try {
            m.setSubMatrix(testData,-1,1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }
        try {
            m.setSubMatrix(testData,1,-1);
            Assert.fail("expecting OutOfRangeException");
        } catch (OutOfRangeException e) {
            
        }

        
        try {
            m.setSubMatrix(null,1,1);
            Assert.fail("expecting NullArgumentException");
        } catch (NullArgumentException e) {
            
        }
        Array2DRowRealMatrix m2 = new Array2DRowRealMatrix();
        try {
            m2.setSubMatrix(testData,0,1);
            Assert.fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }
        try {
            m2.setSubMatrix(testData,1,0);
            Assert.fail("expecting MathIllegalStateException");
        } catch (MathIllegalStateException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{1}, {2, 3}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new double[][] {{}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        RealMatrix m = new Array2DRowRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new Array2DRowRealMatrix(rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(0.0, m.getEntry(i, 0), 0);
            Assert.assertEquals(0.0, m.getEntry(i, columns - 1), 0);
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(0.0, m.getEntry(0, j), 0);
            Assert.assertEquals(0.0, m.getEntry(rows - 1, j), 0);
        }
    }

// org.apache.commons.math3.linear.Array2DRowRealMatrixTest::testSerial
    public void testSerial()  {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(testData);
        Assert.assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testConstructors
    public void testConstructors() {

        ArrayFieldVector<Fraction> v0 = new ArrayFieldVector<Fraction>(FractionField.getInstance());
        Assert.assertEquals(0, v0.getDimension());

        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(FractionField.getInstance(), 7);
        Assert.assertEquals(7, v1.getDimension());
        Assert.assertEquals(new Fraction(0), v1.getEntry(6));

        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(5, new Fraction(123, 100));
        Assert.assertEquals(5, v2.getDimension());
        Assert.assertEquals(new Fraction(123, 100), v2.getEntry(4));

        ArrayFieldVector<Fraction> v3 = new ArrayFieldVector<Fraction>(FractionField.getInstance(), vec1);
        Assert.assertEquals(3, v3.getDimension());
        Assert.assertEquals(new Fraction(2), v3.getEntry(1));

        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(FractionField.getInstance(), vec4, 3, 2);
        Assert.assertEquals(2, v4.getDimension());
        Assert.assertEquals(new Fraction(4), v4.getEntry(0));
        try {
            new ArrayFieldVector<Fraction>(vec4, 8, 3);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        FieldVector<Fraction> v5_i = new ArrayFieldVector<Fraction>(dvec1);
        Assert.assertEquals(9, v5_i.getDimension());
        Assert.assertEquals(new Fraction(9), v5_i.getEntry(8));

        ArrayFieldVector<Fraction> v5 = new ArrayFieldVector<Fraction>(dvec1);
        Assert.assertEquals(9, v5.getDimension());
        Assert.assertEquals(new Fraction(9), v5.getEntry(8));

        ArrayFieldVector<Fraction> v6 = new ArrayFieldVector<Fraction>(dvec1, 3, 2);
        Assert.assertEquals(2, v6.getDimension());
        Assert.assertEquals(new Fraction(4), v6.getEntry(0));
        try {
            new ArrayFieldVector<Fraction>(dvec1, 8, 3);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        ArrayFieldVector<Fraction> v7 = new ArrayFieldVector<Fraction>(v1);
        Assert.assertEquals(7, v7.getDimension());
        Assert.assertEquals(new Fraction(0), v7.getEntry(6));

        FieldVectorTestImpl<Fraction> v7_i = new FieldVectorTestImpl<Fraction>(vec1);

        ArrayFieldVector<Fraction> v7_2 = new ArrayFieldVector<Fraction>(v7_i);
        Assert.assertEquals(3, v7_2.getDimension());
        Assert.assertEquals(new Fraction(2), v7_2.getEntry(1));

        ArrayFieldVector<Fraction> v8 = new ArrayFieldVector<Fraction>(v1, true);
        Assert.assertEquals(7, v8.getDimension());
        Assert.assertEquals(new Fraction(0), v8.getEntry(6));
        Assert.assertNotSame("testData not same object ", v1.getDataRef(), v8.getDataRef());

        ArrayFieldVector<Fraction> v8_2 = new ArrayFieldVector<Fraction>(v1, false);
        Assert.assertEquals(7, v8_2.getDimension());
        Assert.assertEquals(new Fraction(0), v8_2.getEntry(6));
        Assert.assertArrayEquals(v1.getDataRef(), v8_2.getDataRef());

        ArrayFieldVector<Fraction> v9 = new ArrayFieldVector<Fraction>(v1, v3);
        Assert.assertEquals(10, v9.getDimension());
        Assert.assertEquals(new Fraction(1), v9.getEntry(7));

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testDataInOut
    public void testDataInOut() {

        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(vec2);
        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(vec4);
        FieldVectorTestImpl<Fraction> v2_t = new FieldVectorTestImpl<Fraction>(vec2);

        FieldVector<Fraction> v_append_1 = v1.append(v2);
        Assert.assertEquals(6, v_append_1.getDimension());
        Assert.assertEquals(new Fraction(4), v_append_1.getEntry(3));

        FieldVector<Fraction> v_append_2 = v1.append(new Fraction(2));
        Assert.assertEquals(4, v_append_2.getDimension());
        Assert.assertEquals(new Fraction(2), v_append_2.getEntry(3));

        FieldVector<Fraction> v_append_4 = v1.append(v2_t);
        Assert.assertEquals(6, v_append_4.getDimension());
        Assert.assertEquals(new Fraction(4), v_append_4.getEntry(3));

        FieldVector<Fraction> v_copy = v1.copy();
        Assert.assertEquals(3, v_copy.getDimension());
        Assert.assertNotSame("testData not same object ", v1.getDataRef(), v_copy.getData());

        Fraction[] a_frac = v1.toArray();
        Assert.assertEquals(3, a_frac.length);
        Assert.assertNotSame("testData not same object ", v1.getDataRef(), a_frac);

        FieldVector<Fraction> vout5 = v4.getSubVector(3, 3);
        Assert.assertEquals(3, vout5.getDimension());
        Assert.assertEquals(new Fraction(5), vout5.getEntry(1));
        try {
            v4.getSubVector(3, 7);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set1 = (ArrayFieldVector<Fraction>) v1.copy();
        v_set1.setEntry(1, new Fraction(11));
        Assert.assertEquals(new Fraction(11), v_set1.getEntry(1));
        try {
            v_set1.setEntry(3, new Fraction(11));
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set2 = (ArrayFieldVector<Fraction>) v4.copy();
        v_set2.set(3, v1);
        Assert.assertEquals(new Fraction(1), v_set2.getEntry(3));
        Assert.assertEquals(new Fraction(7), v_set2.getEntry(6));
        try {
            v_set2.set(7, v1);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set3 = (ArrayFieldVector<Fraction>) v1.copy();
        v_set3.set(new Fraction(13));
        Assert.assertEquals(new Fraction(13), v_set3.getEntry(2));

        try {
            v_set3.getEntry(23);
            Assert.fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }

        ArrayFieldVector<Fraction> v_set4 = (ArrayFieldVector<Fraction>) v4.copy();
        v_set4.setSubVector(3, v2_t);
        Assert.assertEquals(new Fraction(4), v_set4.getEntry(3));
        Assert.assertEquals(new Fraction(7), v_set4.getEntry(6));
        try {
            v_set4.setSubVector(7, v2_t);
            Assert.fail("OutOfRangeException expected");
        } catch (OutOfRangeException ex) {
            
        }

        ArrayFieldVector<Fraction> vout10 = (ArrayFieldVector<Fraction>) v1.copy();
        ArrayFieldVector<Fraction> vout10_2 = (ArrayFieldVector<Fraction>) v1.copy();
        Assert.assertEquals(vout10, vout10_2);
        vout10_2.setEntry(0, new Fraction(11, 10));
        Assert.assertNotSame(vout10, vout10_2);

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testMapFunctions
    public void testMapFunctions() {
        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);

        
        FieldVector<Fraction> v_mapAdd = v1.mapAdd(new Fraction(2));
        Fraction[] result_mapAdd = {new Fraction(3), new Fraction(4), new Fraction(5)};
        checkArray("compare vectors" ,result_mapAdd,v_mapAdd.getData());

        
        FieldVector<Fraction> v_mapAddToSelf = v1.copy();
        v_mapAddToSelf.mapAddToSelf(new Fraction(2));
        Fraction[] result_mapAddToSelf = {new Fraction(3), new Fraction(4), new Fraction(5)};
        checkArray("compare vectors" ,result_mapAddToSelf,v_mapAddToSelf.getData());

        
        FieldVector<Fraction> v_mapSubtract = v1.mapSubtract(new Fraction(2));
        Fraction[] result_mapSubtract = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        checkArray("compare vectors" ,result_mapSubtract,v_mapSubtract.getData());

        
        FieldVector<Fraction> v_mapSubtractToSelf = v1.copy();
        v_mapSubtractToSelf.mapSubtractToSelf(new Fraction(2));
        Fraction[] result_mapSubtractToSelf = {new Fraction(-1), new Fraction(0), new Fraction(1)};
        checkArray("compare vectors" ,result_mapSubtractToSelf,v_mapSubtractToSelf.getData());

        
        FieldVector<Fraction> v_mapMultiply = v1.mapMultiply(new Fraction(2));
        Fraction[] result_mapMultiply = {new Fraction(2), new Fraction(4), new Fraction(6)};
        checkArray("compare vectors" ,result_mapMultiply,v_mapMultiply.getData());

        
        FieldVector<Fraction> v_mapMultiplyToSelf = v1.copy();
        v_mapMultiplyToSelf.mapMultiplyToSelf(new Fraction(2));
        Fraction[] result_mapMultiplyToSelf = {new Fraction(2), new Fraction(4), new Fraction(6)};
        checkArray("compare vectors" ,result_mapMultiplyToSelf,v_mapMultiplyToSelf.getData());

        
        FieldVector<Fraction> v_mapDivide = v1.mapDivide(new Fraction(2));
        Fraction[] result_mapDivide = {new Fraction(1, 2), new Fraction(1), new Fraction(3, 2)};
        checkArray("compare vectors" ,result_mapDivide,v_mapDivide.getData());

        
        FieldVector<Fraction> v_mapDivideToSelf = v1.copy();
        v_mapDivideToSelf.mapDivideToSelf(new Fraction(2));
        Fraction[] result_mapDivideToSelf = {new Fraction(1, 2), new Fraction(1), new Fraction(3, 2)};
        checkArray("compare vectors" ,result_mapDivideToSelf,v_mapDivideToSelf.getData());

        
        FieldVector<Fraction> v_mapInv = v1.mapInv();
        Fraction[] result_mapInv = {new Fraction(1),new Fraction(1, 2),new Fraction(1, 3)};
        checkArray("compare vectors" ,result_mapInv,v_mapInv.getData());

        
        FieldVector<Fraction> v_mapInvToSelf = v1.copy();
        v_mapInvToSelf.mapInvToSelf();
        Fraction[] result_mapInvToSelf = {new Fraction(1),new Fraction(1, 2),new Fraction(1, 3)};
        checkArray("compare vectors" ,result_mapInvToSelf,v_mapInvToSelf.getData());

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testBasicFunctions
    public void testBasicFunctions() {
        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v2 = new ArrayFieldVector<Fraction>(vec2);
        new ArrayFieldVector<Fraction>(vec_null);

        FieldVectorTestImpl<Fraction> v2_t = new FieldVectorTestImpl<Fraction>(vec2);

        
        ArrayFieldVector<Fraction> v_add = v1.add(v2);
        Fraction[] result_add = {new Fraction(5), new Fraction(7), new Fraction(9)};
        checkArray("compare vect" ,v_add.getData(),result_add);

        FieldVectorTestImpl<Fraction> vt2 = new FieldVectorTestImpl<Fraction>(vec2);
        FieldVector<Fraction> v_add_i = v1.add(vt2);
        Fraction[] result_add_i = {new Fraction(5), new Fraction(7), new Fraction(9)};
        checkArray("compare vect" ,v_add_i.getData(),result_add_i);

        
        ArrayFieldVector<Fraction> v_subtract = v1.subtract(v2);
        Fraction[] result_subtract = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        checkArray("compare vect" ,v_subtract.getData(),result_subtract);

        FieldVector<Fraction> v_subtract_i = v1.subtract(vt2);
        Fraction[] result_subtract_i = {new Fraction(-3), new Fraction(-3), new Fraction(-3)};
        checkArray("compare vect" ,v_subtract_i.getData(),result_subtract_i);

        
        ArrayFieldVector<Fraction>  v_ebeMultiply = v1.ebeMultiply(v2);
        Fraction[] result_ebeMultiply = {new Fraction(4), new Fraction(10), new Fraction(18)};
        checkArray("compare vect" ,v_ebeMultiply.getData(),result_ebeMultiply);

        FieldVector<Fraction>  v_ebeMultiply_2 = v1.ebeMultiply(v2_t);
        Fraction[] result_ebeMultiply_2 = {new Fraction(4), new Fraction(10), new Fraction(18)};
        checkArray("compare vect" ,v_ebeMultiply_2.getData(),result_ebeMultiply_2);

        
        ArrayFieldVector<Fraction>  v_ebeDivide = v1.ebeDivide(v2);
        Fraction[] result_ebeDivide = {new Fraction(1, 4), new Fraction(2, 5), new Fraction(1, 2)};
        checkArray("compare vect" ,v_ebeDivide.getData(),result_ebeDivide);

        FieldVector<Fraction>  v_ebeDivide_2 = v1.ebeDivide(v2_t);
        Fraction[] result_ebeDivide_2 = {new Fraction(1, 4), new Fraction(2, 5), new Fraction(1, 2)};
        checkArray("compare vect" ,v_ebeDivide_2.getData(),result_ebeDivide_2);

        
        Fraction dot =  v1.dotProduct(v2);
        Assert.assertEquals("compare val ",new Fraction(32), dot);

        
        Fraction dot_2 =  v1.dotProduct(v2_t);
        Assert.assertEquals("compare val ",new Fraction(32), dot_2);

        FieldMatrix<Fraction> m_outerProduct = v1.outerProduct(v2);
        Assert.assertEquals("compare val ",new Fraction(4), m_outerProduct.getEntry(0,0));

        FieldMatrix<Fraction> m_outerProduct_2 = v1.outerProduct(v2_t);
        Assert.assertEquals("compare val ",new Fraction(4), m_outerProduct_2.getEntry(0,0));

        ArrayFieldVector<Fraction> v_projection = v1.projection(v2);
        Fraction[] result_projection = {new Fraction(128, 77), new Fraction(160, 77), new Fraction(192, 77)};
        checkArray("compare vect", v_projection.getData(), result_projection);

        FieldVector<Fraction> v_projection_2 = v1.projection(v2_t);
        Fraction[] result_projection_2 = {new Fraction(128, 77), new Fraction(160, 77), new Fraction(192, 77)};
        checkArray("compare vect", v_projection_2.getData(), result_projection_2);

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testMisc
    public void testMisc() {
        ArrayFieldVector<Fraction> v1 = new ArrayFieldVector<Fraction>(vec1);
        ArrayFieldVector<Fraction> v4 = new ArrayFieldVector<Fraction>(vec4);
        FieldVector<Fraction> v4_2 = new ArrayFieldVector<Fraction>(vec4);

        String out1 = v1.toString();
        Assert.assertTrue("some output ",  out1.length()!=0);
        
        try {
            v1.checkVectorDimensions(2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

       try {
            v1.checkVectorDimensions(v4);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        try {
            v1.checkVectorDimensions(v4_2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

    }
