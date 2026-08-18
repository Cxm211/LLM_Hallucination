// buggy code
    public static double linearCombination(final double[] a, final double[] b)
        throws DimensionMismatchException {
        final int len = a.length;
        if (len != b.length) {
            throw new DimensionMismatchException(len, b.length);
        }

            // Revert to scalar multiplication.

        final double[] prodHigh = new double[len];
        double prodLowSum = 0;

        for (int i = 0; i < len; i++) {
            final double ai = a[i];
            final double ca = SPLIT_FACTOR * ai;
            final double aHigh = ca - (ca - ai);
            final double aLow = ai - aHigh;

            final double bi = b[i];
            final double cb = SPLIT_FACTOR * bi;
            final double bHigh = cb - (cb - bi);
            final double bLow = bi - bHigh;
            prodHigh[i] = ai * bi;
            final double prodLow = aLow * bLow - (((prodHigh[i] -
                                                    aHigh * bHigh) -
                                                   aLow * bHigh) -
                                                  aHigh * bLow);
            prodLowSum += prodLow;
        }


        final double prodHighCur = prodHigh[0];
        double prodHighNext = prodHigh[1];
        double sHighPrev = prodHighCur + prodHighNext;
        double sPrime = sHighPrev - prodHighNext;
        double sLowSum = (prodHighNext - (sHighPrev - sPrime)) + (prodHighCur - sPrime);

        final int lenMinusOne = len - 1;
        for (int i = 1; i < lenMinusOne; i++) {
            prodHighNext = prodHigh[i + 1];
            final double sHighCur = sHighPrev + prodHighNext;
            sPrime = sHighCur - prodHighNext;
            sLowSum += (prodHighNext - (sHighCur - sPrime)) + (sHighPrev - sPrime);
            sHighPrev = sHighCur;
        }

        double result = sHighPrev + (prodLowSum + sLowSum);

        if (Double.isNaN(result)) {
            // either we have split infinite numbers or some coefficients were NaNs,
            // just rely on the naive implementation and let IEEE754 handle this
            result = 0;
            for (int i = 0; i < len; ++i) {
                result += a[i] * b[i];
            }
        }

        return result;
    }

// relevant test
// org.apache.commons.math3.distribution.LogNormalDistributionTest::testGetScale
    public void testGetScale() {
        LogNormalDistribution distribution = (LogNormalDistribution)getDistribution();
        Assert.assertEquals(2.1, distribution.getScale(), 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testGetShape
    public void testGetShape() {
        LogNormalDistribution distribution = (LogNormalDistribution)getDistribution();
        Assert.assertEquals(1.4, distribution.getShape(), 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testPreconditions
    public void testPreconditions() {
        new LogNormalDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testDensity
    public void testDensity() {
        double [] x = new double[]{-2, -1, 0, 1, 2};
        
        checkDensity(0, 1, x, new double[] { 0.0000000000, 0.0000000000,
                                             0.0000000000, 0.3989422804,
                                             0.1568740193 });
        
        checkDensity(1.1, 1, x, new double[] { 0.0000000000, 0.0000000000,
                                               0.0000000000, 0.2178521770,
                                               0.1836267118});
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testExtremeValues
    public void testExtremeValues() {
        LogNormalDistribution d = new LogNormalDistribution(0, 1);
        for (int i = 0; i < 1e5; i++) { 
            double upperTail = d.cumulativeProbability(i);
            if (i <= 72) { 
                Assert.assertTrue(upperTail < 1.0d);
            }
            else { 
                Assert.assertTrue(upperTail > 0.99999);
            }
        }

        Assert.assertEquals(d.cumulativeProbability(Double.MAX_VALUE), 1, 0);
        Assert.assertEquals(d.cumulativeProbability(-Double.MAX_VALUE), 0, 0);
        Assert.assertEquals(d.cumulativeProbability(Double.POSITIVE_INFINITY), 1, 0);
        Assert.assertEquals(d.cumulativeProbability(Double.NEGATIVE_INFINITY), 0, 0);
    }

// org.apache.commons.math3.distribution.LogNormalDistributionTest::testMeanVariance
    public void testMeanVariance() {
        final double tol = 1e-9;
        LogNormalDistribution dist;

        dist = new LogNormalDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 1.6487212707001282, tol);
        Assert.assertEquals(dist.getNumericalVariance(),
                            4.670774270471604, tol);

        dist = new LogNormalDistribution(2.2, 1.4);
        Assert.assertEquals(dist.getNumericalMean(), 24.046753552064498, tol);
        Assert.assertEquals(dist.getNumericalVariance(),
                            3526.913651880464, tol);

        dist = new LogNormalDistribution(-2000.9, 10.4);
        Assert.assertEquals(dist.getNumericalMean(), 0.0, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 0.0, tol);
    }

// org.apache.commons.math3.distribution.MultivariateNormalDistributionTest::testGetMean
    public void testGetMean() {
        final double[] mu = { -1.5, 2 };
        final double[][] sigma = { { 2, -1.1 },
                                   { -1.1, 2 } };
        final MultivariateNormalDistribution d = new MultivariateNormalDistribution(mu, sigma);

        final double[] m = d.getMeans();
        for (int i = 0; i < m.length; i++) {
            Assert.assertEquals(mu[i], m[i], 0);
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalDistributionTest::testGetCovarianceMatrix
    public void testGetCovarianceMatrix() {
        final double[] mu = { -1.5, 2 };
        final double[][] sigma = { { 2, -1.1 },
                                   { -1.1, 2 } };
        final MultivariateNormalDistribution d = new MultivariateNormalDistribution(mu, sigma);

        final RealMatrix s = d.getCovariances();
        final int dim = d.getDimension();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                Assert.assertEquals(sigma[i][j], s.getEntry(i, j), 0);
            }
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalDistributionTest::testSampling
    public void testSampling() {
        final double[] mu = { -1.5, 2 };
        final double[][] sigma = { { 2, -1.1 },
                                   { -1.1, 2 } };
        final MultivariateNormalDistribution d = new MultivariateNormalDistribution(mu, sigma);
        d.reseedRandomGenerator(50);

        final int n = 500000;

        final double[][] samples = d.sample(n);
        final int dim = d.getDimension();
        final double[] sampleMeans = new double[dim];

        for (int i = 0; i < samples.length; i++) {
            for (int j = 0; j < dim; j++) {
                sampleMeans[j] += samples[i][j];
            }
        }

        final double sampledValueTolerance = 1e-2;
        for (int j = 0; j < dim; j++) {
            sampleMeans[j] /= samples.length;
            Assert.assertEquals(mu[j], sampleMeans[j], sampledValueTolerance);
        }

        final double[][] sampleSigma = new Covariance(samples).getCovarianceMatrix().getData();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                Assert.assertEquals(sigma[i][j], sampleSigma[i][j], sampledValueTolerance);
            }
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalDistributionTest::testDensities
    public void testDensities() {
        final double[] mu = { -1.5, 2 };
        final double[][] sigma = { { 2, -1.1 },
                                   { -1.1, 2 } };
        final MultivariateNormalDistribution d = new MultivariateNormalDistribution(mu, sigma);

        final double[][] testValues = { { -1.5, 2 },
                                        { 4, 4 },
                                        { 1.5, -2 },
                                        { 0, 0 } };
        final double[] densities = new double[testValues.length];
        for (int i = 0; i < densities.length; i++) {
            densities[i] = d.density(testValues[i]);
        }

        
        final double[] correctDensities = { 0.09528357207691344,
                                            5.80932710124009e-09,
                                            0.001387448895173267,
                                            0.03309922090210541 };

        for (int i = 0; i < testValues.length; i++) {
            Assert.assertEquals(correctDensities[i], densities[i], 1e-16);
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalDistributionTest::testUnivariateDistribution
    public void testUnivariateDistribution() {
        final double[] mu = { -1.5 };
        final double[][] sigma = { { 1 } };
 
        final MultivariateNormalDistribution multi = new MultivariateNormalDistribution(mu, sigma);

        final NormalDistribution uni = new NormalDistribution(mu[0], sigma[0][0]);
        final Random rng = new Random();
        final int numCases = 100;
        final double tol = Math.ulp(1d);
        for (int i = 0; i < numCases; i++) {
            final double v = rng.nextDouble() * 10 - 5;
            Assert.assertEquals(uni.density(v), multi.density(new double[] { v }), tol);
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest::testNonUnitWeightSum
    public void testNonUnitWeightSum() {
        final double[] weights = { 1, 2 };
        final double[][] means = { { -1.5, 2.0 },
                                   { 4.0, 8.2 } };
        final double[][][] covariances = { { { 2.0, -1.1 },
                                             { -1.1, 2.0 } },
                                           { { 3.5, 1.5 },
                                             { 1.5, 3.5 } } };
        final MultivariateNormalMixtureModelDistribution d
            = create(weights, means, covariances);

        final List<Pair<Double, MultivariateNormalDistribution>> comp = d.getComponents();

        Assert.assertEquals(1d / 3, comp.get(0).getFirst(), Math.ulp(1d));
        Assert.assertEquals(2d / 3, comp.get(1).getFirst(), Math.ulp(1d));
    }

// org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest::testWeightSumOverFlow
    public void testWeightSumOverFlow() {
        final double[] weights = { 0.5 * Double.MAX_VALUE, 0.51 * Double.MAX_VALUE };
        final double[][] means = { { -1.5, 2.0 },
                                   { 4.0, 8.2 } };
        final double[][][] covariances = { { { 2.0, -1.1 },
                                             { -1.1, 2.0 } },
                                           { { 3.5, 1.5 },
                                             { 1.5, 3.5 } } };
        final MultivariateNormalMixtureModelDistribution d
            = create(weights, means, covariances);
    }

// org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest::testPreconditionPositiveWeights
    public void testPreconditionPositiveWeights() {
        final double[] negativeWeights = { -0.5, 1.5 };
        final double[][] means = { { -1.5, 2.0 },
                                   { 4.0, 8.2 } };
        final double[][][] covariances = { { { 2.0, -1.1 },
                                             { -1.1, 2.0 } },
                                           { { 3.5, 1.5 },
                                             { 1.5, 3.5 } } };
        final MultivariateNormalMixtureModelDistribution d
            = create(negativeWeights, means, covariances);
    }

// org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest::testDensities
    public void testDensities() {
        final double[] weights = { 0.3, 0.7 };
        final double[][] means = { { -1.5, 2.0 },
                                   { 4.0, 8.2 } };
        final double[][][] covariances = { { { 2.0, -1.1 },
                                             { -1.1, 2.0 } },
                                           { { 3.5, 1.5 },
                                             { 1.5, 3.5 } } };
        final MultivariateNormalMixtureModelDistribution d
            = create(weights, means, covariances);

        
        final double[][] testValues = { { -1.5, 2 },
                                        { 4, 8.2 },
                                        { 1.5, -2 },
                                        { 0, 0 } };

        
        
        
        
        
        final double[] correctDensities = { 0.02862037278930575,
                                            0.03523044847314091,
                                            0.000416241365629767,
                                            0.009932042831700297 };

        for (int i = 0; i < testValues.length; i++) {
            Assert.assertEquals(correctDensities[i], d.density(testValues[i]), Math.ulp(1d));
        }
    }

// org.apache.commons.math3.distribution.MultivariateNormalMixtureModelDistributionTest::testSampling
    public void testSampling() {
        final double[] weights = { 0.3, 0.7 };
        final double[][] means = { { -1.5, 2.0 },
                                   { 4.0, 8.2 } };
        final double[][][] covariances = { { { 2.0, -1.1 },
                                             { -1.1, 2.0 } },
                                           { { 3.5, 1.5 },
                                             { 1.5, 3.5 } } };
        final MultivariateNormalMixtureModelDistribution d
            = create(weights, means, covariances);
        d.reseedRandomGenerator(50);

        final double[][] correctSamples = getCorrectSamples();
        final int n = correctSamples.length;
        final double[][] samples = d.sample(n);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < samples[i].length; j++) {
                Assert.assertEquals(correctSamples[i][j], samples[i][j], 1e-16);
            }
        }
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testQuantiles
    public void testQuantiles() {
        setDensityTestValues(new double[] {0.0385649760808, 0.172836231799, 0.284958771715, 0.172836231799, 0.0385649760808,
                0.00316560600853, 9.55930184035e-05, 1.06194251052e-06});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new NormalDistribution(0, 1));
        setDensityTestValues(new double[] {0.0539909665132, 0.241970724519, 0.398942280401, 0.241970724519, 0.0539909665132,
                0.00443184841194, 0.000133830225765, 1.48671951473e-06});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new NormalDistribution(0, 0.1));
        setDensityTestValues(new double[] {0.539909665132, 2.41970724519, 3.98942280401, 2.41970724519,
                0.539909665132, 0.0443184841194, 0.00133830225765, 1.48671951473e-05});
        verifyQuantiles();
        verifyDensities();
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(
                new double[] {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testGetMean
    public void testGetMean() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        Assert.assertEquals(2.1, distribution.getMean(), 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testGetStandardDeviation
    public void testGetStandardDeviation() {
        NormalDistribution distribution = (NormalDistribution) getDistribution();
        Assert.assertEquals(1.4, distribution.getStandardDeviation(), 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testPreconditions
    public void testPreconditions() {
        new NormalDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testDensity
    public void testDensity() {
        double [] x = new double[]{-2, -1, 0, 1, 2};
        
        checkDensity(0, 1, x, new double[]{0.05399096651, 0.24197072452, 0.39894228040, 0.24197072452, 0.05399096651});
        
        checkDensity(1.1, 1, x, new double[]{0.003266819056,0.043983595980,0.217852177033,0.396952547477,0.266085249899});
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testExtremeValues
    public void testExtremeValues() {
        NormalDistribution distribution = new NormalDistribution(0, 1);
        for (int i = 0; i < 100; i++) { 
            double lowerTail = distribution.cumulativeProbability(-i);
            double upperTail = distribution.cumulativeProbability(i);
            if (i < 9) { 
                
                
                Assert.assertTrue(lowerTail > 0.0d);
                Assert.assertTrue(upperTail < 1.0d);
            }
            else { 
                Assert.assertTrue(lowerTail < 0.00001);
                Assert.assertTrue(upperTail > 0.99999);
            }
        }

        Assert.assertEquals(distribution.cumulativeProbability(Double.MAX_VALUE), 1, 0);
        Assert.assertEquals(distribution.cumulativeProbability(-Double.MAX_VALUE), 0, 0);
        Assert.assertEquals(distribution.cumulativeProbability(Double.POSITIVE_INFINITY), 1, 0);
        Assert.assertEquals(distribution.cumulativeProbability(Double.NEGATIVE_INFINITY), 0, 0);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testMath280
    public void testMath280() {
        NormalDistribution normal = new NormalDistribution(0,1);
        double result = normal.inverseCumulativeProbability(0.9986501019683698);
        Assert.assertEquals(3.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.841344746068543);
        Assert.assertEquals(1.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.9999683287581673);
        Assert.assertEquals(4.0, result, defaultTolerance);
        result = normal.inverseCumulativeProbability(0.9772498680518209);
        Assert.assertEquals(2.0, result, defaultTolerance);
    }

// org.apache.commons.math3.distribution.NormalDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        NormalDistribution dist;

        dist = new NormalDistribution(0, 1);
        Assert.assertEquals(dist.getNumericalMean(), 0, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1, tol);

        dist = new NormalDistribution(2.2, 1.4);
        Assert.assertEquals(dist.getNumericalMean(), 2.2, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 1.4 * 1.4, tol);

        dist = new NormalDistribution(-2000.9, 10.4);
        Assert.assertEquals(dist.getNumericalMean(), -2000.9, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 10.4 * 10.4, tol);
    }

// org.apache.commons.math3.distribution.ParetoDistributionTest::testQuantiles
    public void testQuantiles() {
        setCumulativeTestValues(new double[] {0, 0, 0, 0.510884134236, 0.694625688662, 0.785201995008, 0.837811522357, 0.871634279326});
        setDensityTestValues(new double[] {0, 0, 0.666666666, 0.195646346305, 0.0872498032394, 0.0477328899983, 0.0294888141169, 0.0197485724114});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new ParetoDistribution(1, 1));
        setCumulativeTestValues(new double[] {0, 0, 0, 0.5, 0.666666666667, 0.75, 0.8, 0.833333333333});
        setDensityTestValues(new double[] {0, 0, 1.0, 0.25, 0.111111111111, 0.0625, 0.04, 0.0277777777778});
        verifyQuantiles();
        verifyDensities();

        setDistribution(new ParetoDistribution(0.1, 0.1));
        setCumulativeTestValues(new double[] {0, 0, 0, 0.0669670084632, 0.104041540159, 0.129449436704, 0.148660077479, 0.164041197922});
        setDensityTestValues(new double[] {0, 0, 1.0, 0.466516495768, 0.298652819947, 0.217637640824, 0.170267984504, 0.139326467013});
        verifyQuantiles();
        verifyDensities();
    }

// org.apache.commons.math3.distribution.ParetoDistributionTest::testInverseCumulativeProbabilityExtremes
    public void testInverseCumulativeProbabilityExtremes() {
        setInverseCumulativeTestPoints(new double[] {0, 1});
        setInverseCumulativeTestValues(new double[] {2.1, Double.POSITIVE_INFINITY});
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.ParetoDistributionTest::testGetScale
    public void testGetScale() {
        ParetoDistribution distribution = (ParetoDistribution)getDistribution();
        Assert.assertEquals(2.1, distribution.getScale(), 0);
    }

// org.apache.commons.math3.distribution.ParetoDistributionTest::testGetShape
    public void testGetShape() {
        ParetoDistribution distribution = (ParetoDistribution)getDistribution();
        Assert.assertEquals(1.4, distribution.getShape(), 0);
    }

// org.apache.commons.math3.distribution.ParetoDistributionTest::testPreconditions
    public void testPreconditions() {
        new ParetoDistribution(1, 0);
    }

// org.apache.commons.math3.distribution.ParetoDistributionTest::testDensity
    public void testDensity() {
        double [] x = new double[]{-2, -1, 0, 1, 2};
        
        checkDensity(1, 1, x, new double[] { 0.00, 0.00, 0.00, 1.00, 0.25 });
        
        checkDensity(1.1, 1, x, new double[] { 0.000, 0.000, 0.000, 0.000, 0.275 });
    }

// org.apache.commons.math3.distribution.ParetoDistributionTest::testExtremeValues
    public void testExtremeValues() {
        ParetoDistribution d = new ParetoDistribution(1, 1);
        for (int i = 0; i < 1e5; i++) { 
            double upperTail = d.cumulativeProbability(i);
            if (i <= 1000) { 
                Assert.assertTrue(upperTail < 1.0d);
            }
            else { 
                Assert.assertTrue(upperTail > 0.999);
            }
        }

        Assert.assertEquals(d.cumulativeProbability(Double.MAX_VALUE), 1, 0);
        Assert.assertEquals(d.cumulativeProbability(-Double.MAX_VALUE), 0, 0);
        Assert.assertEquals(d.cumulativeProbability(Double.POSITIVE_INFINITY), 1, 0);
        Assert.assertEquals(d.cumulativeProbability(Double.NEGATIVE_INFINITY), 0, 0);
    }

// org.apache.commons.math3.distribution.ParetoDistributionTest::testMeanVariance
    public void testMeanVariance() {
        final double tol = 1e-9;
        ParetoDistribution dist;

        dist = new ParetoDistribution(1, 1);
        Assert.assertEquals(dist.getNumericalMean(), Double.POSITIVE_INFINITY, tol);
        Assert.assertEquals(dist.getNumericalVariance(), Double.POSITIVE_INFINITY, tol);

        dist = new ParetoDistribution(2.2, 2.4);
        Assert.assertEquals(dist.getNumericalMean(), 3.771428571428, tol);
        Assert.assertEquals(dist.getNumericalVariance(), 14.816326530, tol);
    }

// org.apache.commons.math3.distribution.PascalDistributionTest::testDegenerate0
    public void testDegenerate0() {
        setDistribution(new PascalDistribution(5, 0.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 0d, 0d, 0d, 0d});
        setDensityTestPoints(new int[] {-1, 0, 1, 10, 11});
        setDensityTestValues(new double[] {0d, 0d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.PascalDistributionTest::testDegenerate1
    public void testDegenerate1() {
        setDistribution(new PascalDistribution(5, 1.0d));
        setCumulativeTestPoints(new int[] {-1, 0, 1, 2, 5, 10 });
        setCumulativeTestValues(new double[] {0d, 1d, 1d, 1d, 1d, 1d});
        setDensityTestPoints(new int[] {-1, 0, 1, 2, 5, 10});
        setDensityTestValues(new double[] {0d, 1d, 0d, 0d, 0d, 0d});
        setInverseCumulativeTestPoints(new double[] {0.1d, 0.5d});
        setInverseCumulativeTestValues(new int[] {0, 0});
        verifyDensities();
        verifyCumulativeProbabilities();
        verifyInverseCumulativeProbabilities();
    }

// org.apache.commons.math3.distribution.PascalDistributionTest::testMoments
    public void testMoments() {
        final double tol = 1e-9;
        PascalDistribution dist;

        dist = new PascalDistribution(10, 0.5);
        Assert.assertEquals(dist.getNumericalMean(), ( 10d * 0.5d ) / 0.5d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), ( 10d * 0.5d ) / (0.5d * 0.5d), tol);

        dist = new PascalDistribution(25, 0.7);
        Assert.assertEquals(dist.getNumericalMean(), ( 25d * 0.3d ) / 0.7d, tol);
        Assert.assertEquals(dist.getNumericalVariance(), ( 25d * 0.3d ) / (0.7d * 0.7d), tol);
    }

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

// org.apache.commons.math3.distribution.UniformRealDistributionTest::testInverseCumulativeDistribution
    public void testInverseCumulativeDistribution() {
        UniformRealDistribution dist = new UniformRealDistribution(0, 1e-9);
        
        Assert.assertEquals(2.5e-10, dist.inverseCumulativeProbability(0.25), 0);
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
            new WeibullDistribution(0, 2);
            Assert.fail("NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException e) {
            
        }
    }

// org.apache.commons.math3.distribution.WeibullDistributionTest::testBeta
    public void testBeta() {
        WeibullDistribution dist = new WeibullDistribution(1, 2);
        Assert.assertEquals(2, dist.getScale(), 0);
        try {
            new WeibullDistribution(1, 0);
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

// org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest::testNonEmptyData
    public void testNonEmptyData() {
        
        new MultivariateNormalMixtureExpectationMaximization(new double[][] {});
    }

// org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest::testNonJaggedData
    public void testNonJaggedData() {
        
        double[][] data = new double[][] {
                { 1, 2, 3 },
                { 4, 5, 6, 7 },
        };
        new MultivariateNormalMixtureExpectationMaximization(data);
    }

// org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest::testMultipleColumnsRequired
    public void testMultipleColumnsRequired() {
        
        double[][] data = new double[][] {
                { 1 }, { 2 }
        };
        new MultivariateNormalMixtureExpectationMaximization(data);
    }

// org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest::testMaxIterationsPositive
    public void testMaxIterationsPositive() {
        
        double[][] data = getTestSamples();
        MultivariateNormalMixtureExpectationMaximization fitter =
                new MultivariateNormalMixtureExpectationMaximization(data);

        MixtureMultivariateNormalDistribution
            initialMix = MultivariateNormalMixtureExpectationMaximization.estimate(data, 2);

        fitter.fit(initialMix, 0, 1E-5);
    }

// org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest::testThresholdPositive
    public void testThresholdPositive() {
        
        double[][] data = getTestSamples();
        MultivariateNormalMixtureExpectationMaximization fitter =
                new MultivariateNormalMixtureExpectationMaximization(
                    data);

        MixtureMultivariateNormalDistribution
            initialMix = MultivariateNormalMixtureExpectationMaximization.estimate(data, 2);

        fitter.fit(initialMix, 1000, 0);
    }

// org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest::testConvergenceException
    public void testConvergenceException() {
        
        double[][] data = getTestSamples();
        MultivariateNormalMixtureExpectationMaximization fitter
            = new MultivariateNormalMixtureExpectationMaximization(data);

        MixtureMultivariateNormalDistribution
            initialMix = MultivariateNormalMixtureExpectationMaximization.estimate(data, 2);

        
        fitter.fit(initialMix, 5, 1E-5);
    }

// org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest::testIncompatibleIntialMixture
    public void testIncompatibleIntialMixture() {
        
        double[][] data = new double[][] {
                { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }
        };
        double[] weights = new double[] { 0.5, 0.5 };

        
        
        MultivariateNormalDistribution[] mvns = new MultivariateNormalDistribution[2];

        mvns[0] = new MultivariateNormalDistribution(new double[] {
                        -0.0021722935000328823, 3.5432892936887908 },
                        new double[][] {
                                { 4.537422569229048, 3.5266152281729304 },
                                { 3.5266152281729304, 6.175448814169779 } });
        mvns[1] = new MultivariateNormalDistribution(new double[] {
                        5.090902706507635, 8.68540656355283 }, new double[][] {
                        { 2.886778573963039, 1.5257474543463154 },
                        { 1.5257474543463154, 3.3794567673616918 } });

        
        List<Pair<Double, MultivariateNormalDistribution>> components =
                new ArrayList<Pair<Double, MultivariateNormalDistribution>>();
        components.add(new Pair<Double, MultivariateNormalDistribution>(
                weights[0], mvns[0]));
        components.add(new Pair<Double, MultivariateNormalDistribution>(
                weights[1], mvns[1]));

        MixtureMultivariateNormalDistribution badInitialMix
            = new MixtureMultivariateNormalDistribution(components);

        MultivariateNormalMixtureExpectationMaximization fitter
            = new MultivariateNormalMixtureExpectationMaximization(data);

        fitter.fit(badInitialMix);
    }

// org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest::testInitialMixture
    public void testInitialMixture() {
        
        final double[] correctWeights = new double[] { 0.5, 0.5 };

        final double[][] correctMeans = new double[][] {
            {-0.0021722935000328823, 3.5432892936887908},
            {5.090902706507635, 8.68540656355283},
        };

        final RealMatrix[] correctCovMats = new Array2DRowRealMatrix[2];

        correctCovMats[0] = new Array2DRowRealMatrix(new double[][] {
                { 4.537422569229048, 3.5266152281729304 },
                { 3.5266152281729304, 6.175448814169779 } });

        correctCovMats[1] = new Array2DRowRealMatrix( new double[][] {
                { 2.886778573963039, 1.5257474543463154 },
                { 1.5257474543463154, 3.3794567673616918 } });

        final MultivariateNormalDistribution[] correctMVNs = new
                MultivariateNormalDistribution[2];

        correctMVNs[0] = new MultivariateNormalDistribution(correctMeans[0],
                correctCovMats[0].getData());

        correctMVNs[1] = new MultivariateNormalDistribution(correctMeans[1],
                correctCovMats[1].getData());

        final MixtureMultivariateNormalDistribution initialMix
            = MultivariateNormalMixtureExpectationMaximization.estimate(getTestSamples(), 2);

        int i = 0;
        for (Pair<Double, MultivariateNormalDistribution> component : initialMix
                .getComponents()) {
            Assert.assertEquals(correctWeights[i], component.getFirst(),
                    Math.ulp(1d));
            
            final double[] means = component.getValue().getMeans();
            Assert.assertTrue(Arrays.equals(correctMeans[i], means));
            
            final RealMatrix covMat = component.getValue().getCovariances();
            Assert.assertEquals(correctCovMats[i], covMat);
            i++;
        }
    }

// org.apache.commons.math3.distribution.fitting.MultivariateNormalMixtureExpectationMaximizationTest::testFit
    public void testFit() {
        
        
        final double[][] data = getTestSamples();
        final double correctLogLikelihood = -4.292431006791994;
        final double[] correctWeights = new double[] { 0.2962324189652912, 0.7037675810347089 };
        
        final double[][] correctMeans = new double[][]{
            {-1.4213112715121132, 1.6924690505757753},
            {4.213612224374709, 7.975621325853645}
        };
        
        final RealMatrix[] correctCovMats = new Array2DRowRealMatrix[2];
        correctCovMats[0] = new Array2DRowRealMatrix(new double[][] {
            { 1.739356907285747, -0.5867644251487614 },
            { -0.5867644251487614, 1.0232932029324642 } }
                );
        correctCovMats[1] = new Array2DRowRealMatrix(new double[][] {
            { 4.245384898007161, 2.5797798966382155 },
            { 2.5797798966382155, 3.9200272522448367 } });
        
        final MultivariateNormalDistribution[] correctMVNs = new MultivariateNormalDistribution[2];
        correctMVNs[0] = new MultivariateNormalDistribution(correctMeans[0], correctCovMats[0].getData());
        correctMVNs[1] = new MultivariateNormalDistribution(correctMeans[1], correctCovMats[1].getData());

        MultivariateNormalMixtureExpectationMaximization fitter
            = new MultivariateNormalMixtureExpectationMaximization(data);

        MixtureMultivariateNormalDistribution initialMix
            = MultivariateNormalMixtureExpectationMaximization.estimate(data, 2);
        fitter.fit(initialMix);
        MixtureMultivariateNormalDistribution fittedMix = fitter.getFittedModel();
        List<Pair<Double, MultivariateNormalDistribution>> components = fittedMix.getComponents();

        Assert.assertEquals(correctLogLikelihood,
                            fitter.getLogLikelihood(),
                            Math.ulp(1d));

        int i = 0;
        for (Pair<Double, MultivariateNormalDistribution> component : components) {
            final double weight = component.getFirst();
            final MultivariateNormalDistribution mvn = component.getSecond();
            final double[] mean = mvn.getMeans();
            final RealMatrix covMat = mvn.getCovariances();
            Assert.assertEquals(correctWeights[i], weight, Math.ulp(1d));
            Assert.assertTrue(Arrays.equals(correctMeans[i], mean));
            Assert.assertEquals(correctCovMats[i], covMat);
            i++;
        }
    }

// org.apache.commons.math3.exception.NonMonotonicSequenceExceptionTest::testAccessors
    public void testAccessors() {
        NonMonotonicSequenceException e
            = new NonMonotonicSequenceException(0, -1, 1, MathArrays.OrderDirection.DECREASING, false);
        Assert.assertEquals(0, e.getArgument());
        Assert.assertEquals(-1, e.getPrevious());
        Assert.assertEquals(1, e.getIndex());
        Assert.assertTrue(e.getDirection() == MathArrays.OrderDirection.DECREASING);
        Assert.assertFalse(e.getStrict());

        e = new NonMonotonicSequenceException(-1, 0, 1);
        Assert.assertEquals(-1, e.getArgument());
        Assert.assertEquals(0, e.getPrevious());
        Assert.assertEquals(1, e.getIndex());
        Assert.assertTrue(e.getDirection() == MathArrays.OrderDirection.INCREASING);
        Assert.assertTrue(e.getStrict());
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testIdentity
    public void testIdentity() {

        FieldRotation<DerivativeStructure> r = createRotation(1, 0, 0, 0, false);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 0, 1));
        checkAngle(r.getAngle(), 0);

        r = createRotation(-1, 0, 0, 0, false);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 0, 1));
        checkAngle(r.getAngle(), 0);

        r = createRotation(42, 0, 0, 0, true);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 0, 1));
        checkAngle(r.getAngle(), 0);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testAxisAngle
    public void testAxisAngle() throws MathIllegalArgumentException {

        FieldRotation<DerivativeStructure> r = new FieldRotation<DerivativeStructure>(createAxis(10, 10, 10), createAngle(2 * FastMath.PI / 3));
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 0, 1));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(1, 0, 0));
        double s = 1 / FastMath.sqrt(3);
        checkVector(r.getAxis(), createVector(s, s, s));
        checkAngle(r.getAngle(), 2 * FastMath.PI / 3);

        try {
            new FieldRotation<DerivativeStructure>(createAxis(0, 0, 0), createAngle(2 * FastMath.PI / 3));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
        }

        r = new FieldRotation<DerivativeStructure>(createAxis(0, 0, 1), createAngle(1.5 * FastMath.PI));
        checkVector(r.getAxis(), createVector(0, 0, -1));
        checkAngle(r.getAngle(), 0.5 * FastMath.PI);

        r = new FieldRotation<DerivativeStructure>(createAxis(0, 1, 0), createAngle(FastMath.PI));
        checkVector(r.getAxis(), createVector(0, 1, 0));
        checkAngle(r.getAngle(), FastMath.PI);

        checkVector(createRotation(1, 0, 0, 0, false).getAxis(), createVector(1, 0, 0));

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testRevert
    public void testRevert() {
        double a = 0.001;
        double b = 0.36;
        double c = 0.48;
        double d = 0.8;
        FieldRotation<DerivativeStructure> r = createRotation(a, b, c, d, true);
        double a2 = a * a;
        double b2 = b * b;
        double c2 = c * c;
        double d2 = d * d;
        double den = (a2 + b2 + c2 + d2) * FastMath.sqrt(a2 + b2 + c2 + d2);
        Assert.assertEquals((b2 + c2 + d2) / den, r.getQ0().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(-a * b / den, r.getQ0().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(-a * c / den, r.getQ0().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(-a * d / den, r.getQ0().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        Assert.assertEquals(-b * a / den, r.getQ1().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals((a2 + c2 + d2) / den, r.getQ1().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(-b * c / den, r.getQ1().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(-b * d / den, r.getQ1().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        Assert.assertEquals(-c * a / den, r.getQ2().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(-c * b / den, r.getQ2().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals((a2 + b2 + d2) / den, r.getQ2().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(-c * d / den, r.getQ2().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        Assert.assertEquals(-d * a / den, r.getQ3().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(-d * b / den, r.getQ3().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(-d * c / den, r.getQ3().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals((a2 + b2 + c2) / den, r.getQ3().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        FieldRotation<DerivativeStructure> reverted = r.revert();
        FieldRotation<DerivativeStructure> rrT = r.applyTo(reverted);
        checkRotationDS(rrT, 1, 0, 0, 0);
        Assert.assertEquals(0, rrT.getQ0().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ0().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ0().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ0().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ1().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ1().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ1().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ1().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ2().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ2().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ2().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ2().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ3().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ3().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ3().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(0, rrT.getQ3().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        FieldRotation<DerivativeStructure> rTr = reverted.applyTo(r);
        checkRotationDS(rTr, 1, 0, 0, 0);
        Assert.assertEquals(0, rTr.getQ0().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ0().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ0().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ0().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ1().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ1().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ1().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ1().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ2().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ2().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ2().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ2().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ3().getPartialDerivative(1, 0, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ3().getPartialDerivative(0, 1, 0, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ3().getPartialDerivative(0, 0, 1, 0), 1.0e-15);
        Assert.assertEquals(0, rTr.getQ3().getPartialDerivative(0, 0, 0, 1), 1.0e-15);
        Assert.assertEquals(r.getAngle().getReal(), reverted.getAngle().getReal(), 1.0e-15);
        Assert.assertEquals(-1, FieldVector3D.dotProduct(r.getAxis(), reverted.getAxis()).getReal(), 1.0e-15);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testVectorOnePair
    public void testVectorOnePair() throws MathArithmeticException {

        FieldVector3D<DerivativeStructure> u = createVector(3, 2, 1);
        FieldVector3D<DerivativeStructure> v = createVector(-4, 2, 2);
        FieldRotation<DerivativeStructure> r = new FieldRotation<DerivativeStructure>(u, v);
        checkVector(r.applyTo(u.scalarMultiply(v.getNorm())), v.scalarMultiply(u.getNorm()));

        checkAngle(new FieldRotation<DerivativeStructure>(u, u.negate()).getAngle(), FastMath.PI);

        try {
            new FieldRotation<DerivativeStructure>(u, createVector(0, 0, 0));
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException e) {
            
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testVectorTwoPairs
    public void testVectorTwoPairs() throws MathArithmeticException {

        FieldVector3D<DerivativeStructure> u1 = createVector(3, 0, 0);
        FieldVector3D<DerivativeStructure> u2 = createVector(0, 5, 0);
        FieldVector3D<DerivativeStructure> v1 = createVector(0, 0, 2);
        FieldVector3D<DerivativeStructure> v2 = createVector(-2, 0, 2);
        FieldRotation<DerivativeStructure> r = new FieldRotation<DerivativeStructure>(u1, u2, v1, v2);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(0, 0, 1));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(-1, 0, 0));

        r = new FieldRotation<DerivativeStructure>(u1, u2, u1.negate(), u2.negate());
        FieldVector3D<DerivativeStructure> axis = r.getAxis();
        if (FieldVector3D.dotProduct(axis, createVector(0, 0, 1)).getReal() > 0) {
            checkVector(axis, createVector(0, 0, 1));
        } else {
            checkVector(axis, createVector(0, 0, -1));
        }
        checkAngle(r.getAngle(), FastMath.PI);

        double sqrt = FastMath.sqrt(2) / 2;
        r = new FieldRotation<DerivativeStructure>(createVector(1, 0, 0),  createVector(0, 1, 0),
                           createVector(0.5, 0.5,  sqrt),
                           createVector(0.5, 0.5, -sqrt));
        checkRotationDS(r, sqrt, 0.5, 0.5, 0);

        r = new FieldRotation<DerivativeStructure>(u1, u2, u1, FieldVector3D.crossProduct(u1, u2));
        checkRotationDS(r, sqrt, -sqrt, 0, 0);

        checkRotationDS(new FieldRotation<DerivativeStructure>(u1, u2, u1, u2), 1, 0, 0, 0);

        try {
            new FieldRotation<DerivativeStructure>(u1, u2, createVector(0, 0, 0), v2);
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException e) {
            
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testMatrix
    public void testMatrix()
            throws NotARotationMatrixException {

        try {
            createRotation(new double[][] {
                { 0.0, 1.0, 0.0 },
                { 1.0, 0.0, 0.0 }
            }, 1.0e-7);
            Assert.fail("Expecting NotARotationMatrixException");
        } catch (NotARotationMatrixException nrme) {
            
        }

        try {
            createRotation(new double[][] {
                {  0.445888,  0.797184, -0.407040 },
                {  0.821760, -0.184320,  0.539200 },
                { -0.354816,  0.574912,  0.737280 }
            }, 1.0e-7);
            Assert.fail("Expecting NotARotationMatrixException");
        } catch (NotARotationMatrixException nrme) {
            
        }

        try {
            createRotation(new double[][] {
                {  0.4,  0.8, -0.4 },
                { -0.4,  0.6,  0.7 },
                {  0.8, -0.2,  0.5 }
            }, 1.0e-15);
            Assert.fail("Expecting NotARotationMatrixException");
        } catch (NotARotationMatrixException nrme) {
            
        }

        checkRotationDS(createRotation(new double[][] {
            {  0.445888,  0.797184, -0.407040 },
            { -0.354816,  0.574912,  0.737280 },
            {  0.821760, -0.184320,  0.539200 }
        }, 1.0e-10),
        0.8, 0.288, 0.384, 0.36);

        checkRotationDS(createRotation(new double[][] {
            {  0.539200,  0.737280,  0.407040 },
            {  0.184320, -0.574912,  0.797184 },
            {  0.821760, -0.354816, -0.445888 }
        }, 1.0e-10),
        0.36, 0.8, 0.288, 0.384);

        checkRotationDS(createRotation(new double[][] {
            { -0.445888,  0.797184, -0.407040 },
            {  0.354816,  0.574912,  0.737280 },
            {  0.821760,  0.184320, -0.539200 }
        }, 1.0e-10),
        0.384, 0.36, 0.8, 0.288);

        checkRotationDS(createRotation(new double[][] {
            { -0.539200,  0.737280,  0.407040 },
            { -0.184320, -0.574912,  0.797184 },
            {  0.821760,  0.354816,  0.445888 }
        }, 1.0e-10),
        0.288, 0.384, 0.36, 0.8);

        double[][] m1 = { { 0.0, 1.0, 0.0 },
            { 0.0, 0.0, 1.0 },
            { 1.0, 0.0, 0.0 } };
        FieldRotation<DerivativeStructure> r = createRotation(m1, 1.0e-7);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(0, 0, 1));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 1, 0));

        double[][] m2 = { { 0.83203, -0.55012, -0.07139 },
            { 0.48293,  0.78164, -0.39474 },
            { 0.27296,  0.29396,  0.91602 } };
        r = createRotation(m2, 1.0e-12);

        DerivativeStructure[][] m3 = r.getMatrix();
        double d00 = m2[0][0] - m3[0][0].getReal();
        double d01 = m2[0][1] - m3[0][1].getReal();
        double d02 = m2[0][2] - m3[0][2].getReal();
        double d10 = m2[1][0] - m3[1][0].getReal();
        double d11 = m2[1][1] - m3[1][1].getReal();
        double d12 = m2[1][2] - m3[1][2].getReal();
        double d20 = m2[2][0] - m3[2][0].getReal();
        double d21 = m2[2][1] - m3[2][1].getReal();
        double d22 = m2[2][2] - m3[2][2].getReal();

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
                double m3tm3 = m3[i][0].getReal() * m3[j][0].getReal() +
                               m3[i][1].getReal() * m3[j][1].getReal() +
                               m3[i][2].getReal() * m3[j][2].getReal();
                if (i == j) {
                    Assert.assertTrue(FastMath.abs(m3tm3 - 1.0) < 1.0e-10);
                } else {
                    Assert.assertTrue(FastMath.abs(m3tm3) < 1.0e-10);
                }
            }
        }

        checkVector(r.applyTo(createVector(1, 0, 0)),
                    new FieldVector3D<DerivativeStructure>(m3[0][0], m3[1][0], m3[2][0]));
        checkVector(r.applyTo(createVector(0, 1, 0)),
                    new FieldVector3D<DerivativeStructure>(m3[0][1], m3[1][1], m3[2][1]));
        checkVector(r.applyTo(createVector(0, 0, 1)),
                    new FieldVector3D<DerivativeStructure>(m3[0][2], m3[1][2], m3[2][2]));

        double[][] m4 = { { 1.0,  0.0,  0.0 },
            { 0.0, -1.0,  0.0 },
            { 0.0,  0.0, -1.0 } };
        r = createRotation(m4, 1.0e-7);
        checkAngle(r.getAngle(), FastMath.PI);

        try {
            double[][] m5 = { { 0.0, 0.0, 1.0 },
                { 0.0, 1.0, 0.0 },
                { 1.0, 0.0, 0.0 } };
            r = createRotation(m5, 1.0e-7);
            Assert.fail("got " + r + ", should have caught an exception");
        } catch (NotARotationMatrixException e) {
            
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testAngles
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
                        FieldRotation<DerivativeStructure> r = new FieldRotation<DerivativeStructure>(CardanOrders[i],
                                                      new DerivativeStructure(3, 1, 0, alpha1),
                                                      new DerivativeStructure(3, 1, 1, alpha2),
                                                      new DerivativeStructure(3, 1, 2, alpha3));
                        DerivativeStructure[] angles = r.getAngles(CardanOrders[i]);
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
                        FieldRotation<DerivativeStructure> r = new FieldRotation<DerivativeStructure>(EulerOrders[i],
                                                      new DerivativeStructure(3, 1, 0, alpha1),
                                                      new DerivativeStructure(3, 1, 1, alpha2),
                                                      new DerivativeStructure(3, 1, 2, alpha3));
                        DerivativeStructure[] angles = r.getAngles(EulerOrders[i]);
                        checkAngle(angles[0], alpha1);
                        checkAngle(angles[1], alpha2);
                        checkAngle(angles[2], alpha3);
                    }
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testSingularities
    public void testSingularities() {

        RotationOrder[] CardanOrders = {
            RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
            RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
        };

        double[] singularCardanAngle = { FastMath.PI / 2, -FastMath.PI / 2 };
        for (int i = 0; i < CardanOrders.length; ++i) {
            for (int j = 0; j < singularCardanAngle.length; ++j) {
                FieldRotation<DerivativeStructure> r = new FieldRotation<DerivativeStructure>(CardanOrders[i],
                                              new DerivativeStructure(3, 1, 0, 0.1),
                                              new DerivativeStructure(3, 1, 1, singularCardanAngle[j]),
                                              new DerivativeStructure(3, 1, 2, 0.3));
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
                FieldRotation<DerivativeStructure> r = new FieldRotation<DerivativeStructure>(EulerOrders[i],
                                              new DerivativeStructure(3, 1, 0, 0.1),
                                              new DerivativeStructure(3, 1, 1, singularEulerAngle[j]),
                                              new DerivativeStructure(3, 1, 2, 0.3));
                try {
                    r.getAngles(EulerOrders[i]);
                    Assert.fail("an exception should have been caught");
                } catch (CardanEulerSingularityException cese) {
                    
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testQuaternion
    public void testQuaternion() throws MathIllegalArgumentException {

        FieldRotation<DerivativeStructure> r1 = new FieldRotation<DerivativeStructure>(createVector(2, -3, 5), createAngle(1.7));
        double n = 23.5;
        FieldRotation<DerivativeStructure> r2 = new FieldRotation<DerivativeStructure>(r1.getQ0().multiply(n), r1.getQ1().multiply(n),
                                       r1.getQ2().multiply(n), r1.getQ3().multiply(n),
                                       true);
        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<DerivativeStructure> u = createVector(x, y, z);
                    checkVector(r2.applyTo(u), r1.applyTo(u));
                }
            }
        }

        r1 = createRotation(0.288,  0.384,  0.36,  0.8, false);
        checkRotationDS(r1,
                        -r1.getQ0().getReal(), -r1.getQ1().getReal(),
                        -r1.getQ2().getReal(), -r1.getQ3().getReal());
        Assert.assertEquals(0.288, r1.toRotation().getQ0(), 1.0e-15);
        Assert.assertEquals(0.384, r1.toRotation().getQ1(), 1.0e-15);
        Assert.assertEquals(0.36,  r1.toRotation().getQ2(), 1.0e-15);
        Assert.assertEquals(0.8,   r1.toRotation().getQ3(), 1.0e-15);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testCompose
    public void testCompose() throws MathIllegalArgumentException {

        FieldRotation<DerivativeStructure> r1       = new FieldRotation<DerivativeStructure>(createVector(2, -3, 5), createAngle(1.7));
        FieldRotation<DerivativeStructure> r2       = new FieldRotation<DerivativeStructure>(createVector(-1, 3, 2), createAngle(0.3));
        FieldRotation<DerivativeStructure> r3       = r2.applyTo(r1);
        FieldRotation<DerivativeStructure> r3Double = r2.applyTo(new Rotation(r1.getQ0().getReal(),
                                                      r1.getQ1().getReal(),
                                                      r1.getQ2().getReal(),
                                                      r1.getQ3().getReal(),
                                                      false));

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<DerivativeStructure> u = createVector(x, y, z);
                    checkVector(r2.applyTo(r1.applyTo(u)), r3.applyTo(u));
                    checkVector(r2.applyTo(r1.applyTo(u)), r3Double.applyTo(u));
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testComposeInverse
    public void testComposeInverse() throws MathIllegalArgumentException {

        FieldRotation<DerivativeStructure> r1 = new FieldRotation<DerivativeStructure>(createVector(2, -3, 5), createAngle(1.7));
        FieldRotation<DerivativeStructure> r2 = new FieldRotation<DerivativeStructure>(createVector(-1, 3, 2), createAngle(0.3));
        FieldRotation<DerivativeStructure> r3 = r2.applyInverseTo(r1);
        FieldRotation<DerivativeStructure> r3Double = r2.applyInverseTo(new Rotation(r1.getQ0().getReal(),
                                                             r1.getQ1().getReal(),
                                                             r1.getQ2().getReal(),
                                                             r1.getQ3().getReal(),
                                                             false));

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<DerivativeStructure> u = createVector(x, y, z);
                    checkVector(r2.applyInverseTo(r1.applyTo(u)), r3.applyTo(u));
                    checkVector(r2.applyInverseTo(r1.applyTo(u)), r3Double.applyTo(u));
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testDoubleVectors
    public void testDoubleVectors() throws MathIllegalArgumentException {

        Well1024a random = new Well1024a(0x180b41cfeeffaf67l);
        UnitSphereRandomVectorGenerator g = new UnitSphereRandomVectorGenerator(3, random);
        for (int i = 0; i < 10; ++i) {
            double[] unit = g.nextVector();
            FieldRotation<DerivativeStructure> r = new FieldRotation<DerivativeStructure>(createVector(unit[0], unit[1], unit[2]),
                                          createAngle(random.nextDouble()));

            for (double x = -0.9; x < 0.9; x += 0.2) {
                for (double y = -0.9; y < 0.9; y += 0.2) {
                    for (double z = -0.9; z < 0.9; z += 0.2) {
                        FieldVector3D<DerivativeStructure> uds   = createVector(x, y, z);
                        FieldVector3D<DerivativeStructure> ruds  = r.applyTo(uds);
                        FieldVector3D<DerivativeStructure> rIuds = r.applyInverseTo(uds);
                        Vector3D   u     = new Vector3D(x, y, z);
                        FieldVector3D<DerivativeStructure> ru    = r.applyTo(u);
                        FieldVector3D<DerivativeStructure> rIu   = r.applyInverseTo(u);
                        DerivativeStructure[] ruArray = new DerivativeStructure[3];
                        r.applyTo(new double[] { x, y, z}, ruArray);
                        DerivativeStructure[] rIuArray = new DerivativeStructure[3];
                        r.applyInverseTo(new double[] { x, y, z}, rIuArray);
                        checkVector(ruds, ru);
                        checkVector(ruds, new FieldVector3D<DerivativeStructure>(ruArray));
                        checkVector(rIuds, rIu);
                        checkVector(rIuds, new FieldVector3D<DerivativeStructure>(rIuArray));
                    }
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testDoubleRotations
    public void testDoubleRotations() throws MathIllegalArgumentException {

        Well1024a random = new Well1024a(0x180b41cfeeffaf67l);
        UnitSphereRandomVectorGenerator g = new UnitSphereRandomVectorGenerator(3, random);
        for (int i = 0; i < 10; ++i) {
            double[] unit1 = g.nextVector();
            Rotation r1 = new Rotation(new Vector3D(unit1[0], unit1[1], unit1[2]),
                                      random.nextDouble());
            FieldRotation<DerivativeStructure> r1Prime = new FieldRotation<DerivativeStructure>(new DerivativeStructure(4, 1, 0, r1.getQ0()),
                                                new DerivativeStructure(4, 1, 1, r1.getQ1()),
                                                new DerivativeStructure(4, 1, 2, r1.getQ2()),
                                                new DerivativeStructure(4, 1, 3, r1.getQ3()),
                                                false);
            double[] unit2 = g.nextVector();
            FieldRotation<DerivativeStructure> r2 = new FieldRotation<DerivativeStructure>(createVector(unit2[0], unit2[1], unit2[2]),
                                           createAngle(random.nextDouble()));

            FieldRotation<DerivativeStructure> rA = FieldRotation.applyTo(r1, r2);
            FieldRotation<DerivativeStructure> rB = r1Prime.applyTo(r2);
            FieldRotation<DerivativeStructure> rC = FieldRotation.applyInverseTo(r1, r2);
            FieldRotation<DerivativeStructure> rD = r1Prime.applyInverseTo(r2);

            for (double x = -0.9; x < 0.9; x += 0.2) {
                for (double y = -0.9; y < 0.9; y += 0.2) {
                    for (double z = -0.9; z < 0.9; z += 0.2) {

                        FieldVector3D<DerivativeStructure> uds   = createVector(x, y, z);
                        checkVector(r1Prime.applyTo(uds), FieldRotation.applyTo(r1, uds));
                        checkVector(r1Prime.applyInverseTo(uds), FieldRotation.applyInverseTo(r1, uds));
                        checkVector(rA.applyTo(uds), rB.applyTo(uds));
                        checkVector(rA.applyInverseTo(uds), rB.applyInverseTo(uds));
                        checkVector(rC.applyTo(uds), rD.applyTo(uds));
                        checkVector(rC.applyInverseTo(uds), rD.applyInverseTo(uds));

                    }
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testDerivatives
    public void testDerivatives() {

        double eps      = 5.0e-16;
        double kx       = 2;
        double ky       = -3;
        double kz       = 5;
        double n2       = kx * kx + ky * ky + kz * kz;
        double n        = FastMath.sqrt(n2);
        double theta    = 1.7;
        double cosTheta = FastMath.cos(theta);
        double sinTheta = FastMath.sin(theta);
        FieldRotation<DerivativeStructure> r    = new FieldRotation<DerivativeStructure>(createAxis(kx, ky, kz), createAngle(theta));
        Vector3D a      = new Vector3D(kx / n, ky / n, kz / n);

        
        RealMatrix dadk = MatrixUtils.createRealMatrix(new double[][] {
            { (ky * ky + kz * kz) / ( n * n2),            -kx * ky / ( n * n2),            -kx * kz / ( n * n2) },
            {            -kx * ky / ( n * n2), (kx * kx + kz * kz) / ( n * n2),            -ky * kz / ( n * n2) },
            {            -kx * kz / ( n * n2),            -ky * kz / ( n * n2), (kx * kx + ky * ky) / ( n * n2) }
        });

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    Vector3D   u = new Vector3D(x, y, z);
                    FieldVector3D<DerivativeStructure> v = r.applyTo(createVector(x, y, z));

                    
                    double dot     = Vector3D.dotProduct(u, a);
                    Vector3D cross = Vector3D.crossProduct(a, u);
                    double c1      = 1 - cosTheta;
                    double c2      = c1 * dot;
                    Vector3D rt    = new Vector3D(cosTheta, u, c2, a, sinTheta, cross);
                    Assert.assertEquals(rt.getX(), v.getX().getReal(), eps);
                    Assert.assertEquals(rt.getY(), v.getY().getReal(), eps);
                    Assert.assertEquals(rt.getZ(), v.getZ().getReal(), eps);

                    
                    
                    RealMatrix dvda = MatrixUtils.createRealMatrix(new double[][] {
                        { c1 * x * a.getX() + c2,           c1 * y * a.getX() + sinTheta * z, c1 * z * a.getX() - sinTheta * y },
                        { c1 * x * a.getY() - sinTheta * z, c1 * y * a.getY() + c2,           c1 * z * a.getY() + sinTheta * x },
                        { c1 * x * a.getZ() + sinTheta * y, c1 * y * a.getZ() - sinTheta * x, c1 * z * a.getZ() + c2           }
                    });

                    
                    RealMatrix dvdk = dvda.multiply(dadk);

                    
                    Assert.assertEquals(dvdk.getEntry(0, 0), v.getX().getPartialDerivative(1, 0, 0, 0), eps);
                    Assert.assertEquals(dvdk.getEntry(0, 1), v.getX().getPartialDerivative(0, 1, 0, 0), eps);
                    Assert.assertEquals(dvdk.getEntry(0, 2), v.getX().getPartialDerivative(0, 0, 1, 0), eps);
                    Assert.assertEquals(dvdk.getEntry(1, 0), v.getY().getPartialDerivative(1, 0, 0, 0), eps);
                    Assert.assertEquals(dvdk.getEntry(1, 1), v.getY().getPartialDerivative(0, 1, 0, 0), eps);
                    Assert.assertEquals(dvdk.getEntry(1, 2), v.getY().getPartialDerivative(0, 0, 1, 0), eps);
                    Assert.assertEquals(dvdk.getEntry(2, 0), v.getZ().getPartialDerivative(1, 0, 0, 0), eps);
                    Assert.assertEquals(dvdk.getEntry(2, 1), v.getZ().getPartialDerivative(0, 1, 0, 0), eps);
                    Assert.assertEquals(dvdk.getEntry(2, 2), v.getZ().getPartialDerivative(0, 0, 1, 0), eps);

                    
                    
                    Vector3D dvdTheta =
                            new Vector3D(-sinTheta, u, sinTheta * dot, a, cosTheta, cross);
                    Assert.assertEquals(dvdTheta.getX(), v.getX().getPartialDerivative(0, 0, 0, 1), eps);
                    Assert.assertEquals(dvdTheta.getY(), v.getY().getPartialDerivative(0, 0, 0, 1), eps);
                    Assert.assertEquals(dvdTheta.getZ(), v.getZ().getPartialDerivative(0, 0, 0, 1), eps);

                }
            }
        }
     }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testArray
    public void testArray() throws MathIllegalArgumentException {

        FieldRotation<DerivativeStructure> r = new FieldRotation<DerivativeStructure>(createAxis(2, -3, 5), createAngle(1.7));

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<DerivativeStructure> u = createVector(x, y, z);
                    FieldVector3D<DerivativeStructure> v = r.applyTo(u);
                    DerivativeStructure[] out = new DerivativeStructure[3];
                    r.applyTo(new DerivativeStructure[] { u.getX(), u.getY(), u.getZ() }, out);
                    Assert.assertEquals(v.getX().getReal(), out[0].getReal(), 1.0e-10);
                    Assert.assertEquals(v.getY().getReal(), out[1].getReal(), 1.0e-10);
                    Assert.assertEquals(v.getZ().getReal(), out[2].getReal(), 1.0e-10);
                    r.applyInverseTo(out, out);
                    Assert.assertEquals(u.getX().getReal(), out[0].getReal(), 1.0e-10);
                    Assert.assertEquals(u.getY().getReal(), out[1].getReal(), 1.0e-10);
                    Assert.assertEquals(u.getZ().getReal(), out[2].getReal(), 1.0e-10);
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testApplyInverseTo
    public void testApplyInverseTo() throws MathIllegalArgumentException {

        DerivativeStructure[] in      = new DerivativeStructure[3];
        DerivativeStructure[] out     = new DerivativeStructure[3];
        DerivativeStructure[] rebuilt = new DerivativeStructure[3];
        FieldRotation<DerivativeStructure> r = new FieldRotation<DerivativeStructure>(createVector(2, -3, 5), createAngle(1.7));
        for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
            for (double phi = -1.55; phi < 1.55; phi += 0.2) {
                FieldVector3D<DerivativeStructure> u = createVector(FastMath.cos(lambda) * FastMath.cos(phi),
                                          FastMath.sin(lambda) * FastMath.cos(phi),
                                          FastMath.sin(phi));
                r.applyInverseTo(r.applyTo(u));
                checkVector(u, r.applyInverseTo(r.applyTo(u)));
                checkVector(u, r.applyTo(r.applyInverseTo(u)));
                in[0] = u.getX();
                in[1] = u.getY();
                in[2] = u.getZ();
                r.applyTo(in, out);
                r.applyInverseTo(out, rebuilt);
                Assert.assertEquals(in[0].getReal(), rebuilt[0].getReal(), 1.0e-12);
                Assert.assertEquals(in[1].getReal(), rebuilt[1].getReal(), 1.0e-12);
                Assert.assertEquals(in[2].getReal(), rebuilt[2].getReal(), 1.0e-12);
            }
        }

        r = createRotation(1, 0, 0, 0, false);
        for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
            for (double phi = -1.55; phi < 1.55; phi += 0.2) {
                FieldVector3D<DerivativeStructure> u = createVector(FastMath.cos(lambda) * FastMath.cos(phi),
                                          FastMath.sin(lambda) * FastMath.cos(phi),
                                          FastMath.sin(phi));
                checkVector(u, r.applyInverseTo(r.applyTo(u)));
                checkVector(u, r.applyTo(r.applyInverseTo(u)));
            }
        }

        r = new FieldRotation<DerivativeStructure>(createVector(0, 0, 1), createAngle(FastMath.PI));
        for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
            for (double phi = -1.55; phi < 1.55; phi += 0.2) {
                FieldVector3D<DerivativeStructure> u = createVector(FastMath.cos(lambda) * FastMath.cos(phi),
                                          FastMath.sin(lambda) * FastMath.cos(phi),
                                          FastMath.sin(phi));
                checkVector(u, r.applyInverseTo(r.applyTo(u)));
                checkVector(u, r.applyTo(r.applyInverseTo(u)));
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testIssue639
    public void testIssue639() throws MathArithmeticException{
        FieldVector3D<DerivativeStructure> u1 = createVector(-1321008684645961.0 /  268435456.0,
                                   -5774608829631843.0 /  268435456.0,
                                   -3822921525525679.0 / 4294967296.0);
        FieldVector3D<DerivativeStructure> u2 =createVector( -5712344449280879.0 /    2097152.0,
                                   -2275058564560979.0 /    1048576.0,
                                   4423475992255071.0 /      65536.0);
        FieldRotation<DerivativeStructure> rot = new FieldRotation<DerivativeStructure>(u1, u2, createVector(1, 0, 0),createVector(0, 0, 1));
        Assert.assertEquals( 0.6228370359608200639829222, rot.getQ0().getReal(), 1.0e-15);
        Assert.assertEquals( 0.0257707621456498790029987, rot.getQ1().getReal(), 1.0e-15);
        Assert.assertEquals(-0.0000000002503012255839931, rot.getQ2().getReal(), 1.0e-15);
        Assert.assertEquals(-0.7819270390861109450724902, rot.getQ3().getReal(), 1.0e-15);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDSTest::testIssue801
    public void testIssue801() throws MathArithmeticException {
        FieldVector3D<DerivativeStructure> u1 = createVector(0.9999988431610581, -0.0015210774290851095, 0.0);
        FieldVector3D<DerivativeStructure> u2 = createVector(0.0, 0.0, 1.0);

        FieldVector3D<DerivativeStructure> v1 = createVector(0.9999999999999999, 0.0, 0.0);
        FieldVector3D<DerivativeStructure> v2 = createVector(0.0, 0.0, -1.0);

        FieldRotation<DerivativeStructure> quat = new FieldRotation<DerivativeStructure>(u1, u2, v1, v2);
        double q2 = quat.getQ0().getReal() * quat.getQ0().getReal() +
                    quat.getQ1().getReal() * quat.getQ1().getReal() +
                    quat.getQ2().getReal() * quat.getQ2().getReal() +
                    quat.getQ3().getReal() * quat.getQ3().getReal();
        Assert.assertEquals(1.0, q2, 1.0e-14);
        Assert.assertEquals(0.0, FieldVector3D.angle(v1, quat.applyTo(u1)).getReal(), 1.0e-14);
        Assert.assertEquals(0.0, FieldVector3D.angle(v2, quat.applyTo(u2)).getReal(), 1.0e-14);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testIdentity
    public void testIdentity() {

        FieldRotation<Dfp> r = createRotation(1, 0, 0, 0, false);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 0, 1));
        checkAngle(r.getAngle(), 0);

        r = createRotation(-1, 0, 0, 0, false);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 0, 1));
        checkAngle(r.getAngle(), 0);

        r = createRotation(42, 0, 0, 0, true);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 0, 1));
        checkAngle(r.getAngle(), 0);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testAxisAngle
    public void testAxisAngle() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r = new FieldRotation<Dfp>(createAxis(10, 10, 10), createAngle(2 * FastMath.PI / 3));
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(0, 1, 0));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(0, 0, 1));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(1, 0, 0));
        double s = 1 / FastMath.sqrt(3);
        checkVector(r.getAxis(), createVector(s, s, s));
        checkAngle(r.getAngle(), 2 * FastMath.PI / 3);

        try {
            new FieldRotation<Dfp>(createAxis(0, 0, 0), createAngle(2 * FastMath.PI / 3));
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
        }

        r = new FieldRotation<Dfp>(createAxis(0, 0, 1), createAngle(1.5 * FastMath.PI));
        checkVector(r.getAxis(), createVector(0, 0, -1));
        checkAngle(r.getAngle(), 0.5 * FastMath.PI);

        r = new FieldRotation<Dfp>(createAxis(0, 1, 0), createAngle(FastMath.PI));
        checkVector(r.getAxis(), createVector(0, 1, 0));
        checkAngle(r.getAngle(), FastMath.PI);

        checkVector(createRotation(1, 0, 0, 0, false).getAxis(), createVector(1, 0, 0));

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testRevert
    public void testRevert() {
        double a = 0.001;
        double b = 0.36;
        double c = 0.48;
        double d = 0.8;
        FieldRotation<Dfp> r = createRotation(a, b, c, d, true);
        FieldRotation<Dfp> reverted = r.revert();
        FieldRotation<Dfp> rrT = r.applyTo(reverted);
        checkRotationDS(rrT, 1, 0, 0, 0);
        FieldRotation<Dfp> rTr = reverted.applyTo(r);
        checkRotationDS(rTr, 1, 0, 0, 0);
        Assert.assertEquals(r.getAngle().getReal(), reverted.getAngle().getReal(), 1.0e-15);
        Assert.assertEquals(-1, FieldVector3D.dotProduct(r.getAxis(), reverted.getAxis()).getReal(), 1.0e-15);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testVectorOnePair
    public void testVectorOnePair() throws MathArithmeticException {

        FieldVector3D<Dfp> u = createVector(3, 2, 1);
        FieldVector3D<Dfp> v = createVector(-4, 2, 2);
        FieldRotation<Dfp> r = new FieldRotation<Dfp>(u, v);
        checkVector(r.applyTo(u.scalarMultiply(v.getNorm())), v.scalarMultiply(u.getNorm()));

        checkAngle(new FieldRotation<Dfp>(u, u.negate()).getAngle(), FastMath.PI);

        try {
            new FieldRotation<Dfp>(u, createVector(0, 0, 0));
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException e) {
            
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testVectorTwoPairs
    public void testVectorTwoPairs() throws MathArithmeticException {

        FieldVector3D<Dfp> u1 = createVector(3, 0, 0);
        FieldVector3D<Dfp> u2 = createVector(0, 5, 0);
        FieldVector3D<Dfp> v1 = createVector(0, 0, 2);
        FieldVector3D<Dfp> v2 = createVector(-2, 0, 2);
        FieldRotation<Dfp> r = new FieldRotation<Dfp>(u1, u2, v1, v2);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(0, 0, 1));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(-1, 0, 0));

        r = new FieldRotation<Dfp>(u1, u2, u1.negate(), u2.negate());
        FieldVector3D<Dfp> axis = r.getAxis();
        if (FieldVector3D.dotProduct(axis, createVector(0, 0, 1)).getReal() > 0) {
            checkVector(axis, createVector(0, 0, 1));
        } else {
            checkVector(axis, createVector(0, 0, -1));
        }
        checkAngle(r.getAngle(), FastMath.PI);

        double sqrt = FastMath.sqrt(2) / 2;
        r = new FieldRotation<Dfp>(createVector(1, 0, 0),  createVector(0, 1, 0),
                           createVector(0.5, 0.5,  sqrt),
                           createVector(0.5, 0.5, -sqrt));
        checkRotationDS(r, sqrt, 0.5, 0.5, 0);

        r = new FieldRotation<Dfp>(u1, u2, u1, FieldVector3D.crossProduct(u1, u2));
        checkRotationDS(r, sqrt, -sqrt, 0, 0);

        checkRotationDS(new FieldRotation<Dfp>(u1, u2, u1, u2), 1, 0, 0, 0);

        try {
            new FieldRotation<Dfp>(u1, u2, createVector(0, 0, 0), v2);
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException e) {
            
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testMatrix
    public void testMatrix()
            throws NotARotationMatrixException {

        try {
            createRotation(new double[][] {
                { 0.0, 1.0, 0.0 },
                { 1.0, 0.0, 0.0 }
            }, 1.0e-7);
            Assert.fail("Expecting NotARotationMatrixException");
        } catch (NotARotationMatrixException nrme) {
            
        }

        try {
            createRotation(new double[][] {
                {  0.445888,  0.797184, -0.407040 },
                {  0.821760, -0.184320,  0.539200 },
                { -0.354816,  0.574912,  0.737280 }
            }, 1.0e-7);
            Assert.fail("Expecting NotARotationMatrixException");
        } catch (NotARotationMatrixException nrme) {
            
        }

        try {
            createRotation(new double[][] {
                {  0.4,  0.8, -0.4 },
                { -0.4,  0.6,  0.7 },
                {  0.8, -0.2,  0.5 }
            }, 1.0e-15);
            Assert.fail("Expecting NotARotationMatrixException");
        } catch (NotARotationMatrixException nrme) {
            
        }

        checkRotationDS(createRotation(new double[][] {
            {  0.445888,  0.797184, -0.407040 },
            { -0.354816,  0.574912,  0.737280 },
            {  0.821760, -0.184320,  0.539200 }
        }, 1.0e-10),
        0.8, 0.288, 0.384, 0.36);

        checkRotationDS(createRotation(new double[][] {
            {  0.539200,  0.737280,  0.407040 },
            {  0.184320, -0.574912,  0.797184 },
            {  0.821760, -0.354816, -0.445888 }
        }, 1.0e-10),
        0.36, 0.8, 0.288, 0.384);

        checkRotationDS(createRotation(new double[][] {
            { -0.445888,  0.797184, -0.407040 },
            {  0.354816,  0.574912,  0.737280 },
            {  0.821760,  0.184320, -0.539200 }
        }, 1.0e-10),
        0.384, 0.36, 0.8, 0.288);

        checkRotationDS(createRotation(new double[][] {
            { -0.539200,  0.737280,  0.407040 },
            { -0.184320, -0.574912,  0.797184 },
            {  0.821760,  0.354816,  0.445888 }
        }, 1.0e-10),
        0.288, 0.384, 0.36, 0.8);

        double[][] m1 = { { 0.0, 1.0, 0.0 },
            { 0.0, 0.0, 1.0 },
            { 1.0, 0.0, 0.0 } };
        FieldRotation<Dfp> r = createRotation(m1, 1.0e-7);
        checkVector(r.applyTo(createVector(1, 0, 0)), createVector(0, 0, 1));
        checkVector(r.applyTo(createVector(0, 1, 0)), createVector(1, 0, 0));
        checkVector(r.applyTo(createVector(0, 0, 1)), createVector(0, 1, 0));

        double[][] m2 = { { 0.83203, -0.55012, -0.07139 },
            { 0.48293,  0.78164, -0.39474 },
            { 0.27296,  0.29396,  0.91602 } };
        r = createRotation(m2, 1.0e-12);

        Dfp[][] m3 = r.getMatrix();
        double d00 = m2[0][0] - m3[0][0].getReal();
        double d01 = m2[0][1] - m3[0][1].getReal();
        double d02 = m2[0][2] - m3[0][2].getReal();
        double d10 = m2[1][0] - m3[1][0].getReal();
        double d11 = m2[1][1] - m3[1][1].getReal();
        double d12 = m2[1][2] - m3[1][2].getReal();
        double d20 = m2[2][0] - m3[2][0].getReal();
        double d21 = m2[2][1] - m3[2][1].getReal();
        double d22 = m2[2][2] - m3[2][2].getReal();

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
                double m3tm3 = m3[i][0].getReal() * m3[j][0].getReal() +
                               m3[i][1].getReal() * m3[j][1].getReal() +
                               m3[i][2].getReal() * m3[j][2].getReal();
                if (i == j) {
                    Assert.assertTrue(FastMath.abs(m3tm3 - 1.0) < 1.0e-10);
                } else {
                    Assert.assertTrue(FastMath.abs(m3tm3) < 1.0e-10);
                }
            }
        }

        checkVector(r.applyTo(createVector(1, 0, 0)),
                    new FieldVector3D<Dfp>(m3[0][0], m3[1][0], m3[2][0]));
        checkVector(r.applyTo(createVector(0, 1, 0)),
                    new FieldVector3D<Dfp>(m3[0][1], m3[1][1], m3[2][1]));
        checkVector(r.applyTo(createVector(0, 0, 1)),
                    new FieldVector3D<Dfp>(m3[0][2], m3[1][2], m3[2][2]));

        double[][] m4 = { { 1.0,  0.0,  0.0 },
            { 0.0, -1.0,  0.0 },
            { 0.0,  0.0, -1.0 } };
        r = createRotation(m4, 1.0e-7);
        checkAngle(r.getAngle(), FastMath.PI);

        try {
            double[][] m5 = { { 0.0, 0.0, 1.0 },
                { 0.0, 1.0, 0.0 },
                { 1.0, 0.0, 0.0 } };
            r = createRotation(m5, 1.0e-7);
            Assert.fail("got " + r + ", should have caught an exception");
        } catch (NotARotationMatrixException e) {
            
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testAngles
    public void testAngles()
            throws CardanEulerSingularityException {

        DfpField field = new DfpField(15);

        RotationOrder[] CardanOrders = {
            RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
            RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
        };

        for (int i = 0; i < CardanOrders.length; ++i) {
            for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 2.0) {
                for (double alpha2 = -1.55; alpha2 < 1.55; alpha2 += 0.8) {
                    for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 2.0) {
                        FieldRotation<Dfp> r = new FieldRotation<Dfp>(CardanOrders[i],
                                                                      field.newDfp(alpha1),
                                                                      field.newDfp(alpha2),
                                                                      field.newDfp(alpha3));
                        Dfp[] angles = r.getAngles(CardanOrders[i]);
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
            for (double alpha1 = 0.1; alpha1 < 6.2; alpha1 += 2.0) {
                for (double alpha2 = 0.05; alpha2 < 3.1; alpha2 += 0.8) {
                    for (double alpha3 = 0.1; alpha3 < 6.2; alpha3 += 2.0) {
                        FieldRotation<Dfp> r = new FieldRotation<Dfp>(EulerOrders[i],
                                                                      field.newDfp(alpha1),
                                                                      field.newDfp(alpha2),
                                                                      field.newDfp(alpha3));
                        Dfp[] angles = r.getAngles(EulerOrders[i]);
                        checkAngle(angles[0], alpha1);
                        checkAngle(angles[1], alpha2);
                        checkAngle(angles[2], alpha3);
                    }
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testSingularities
    public void testSingularities() {

        DfpField field = new DfpField(20);
        RotationOrder[] CardanOrders = {
            RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
            RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
        };

        double[] singularCardanAngle = { FastMath.PI / 2, -FastMath.PI / 2 };
        for (int i = 0; i < CardanOrders.length; ++i) {
            for (int j = 0; j < singularCardanAngle.length; ++j) {
                FieldRotation<Dfp> r = new FieldRotation<Dfp>(CardanOrders[i],
                                                              field.newDfp(0.1),
                                                              field.newDfp(singularCardanAngle[j]),
                                                              field.newDfp(0.3));
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
                FieldRotation<Dfp> r = new FieldRotation<Dfp>(EulerOrders[i],
                                                              field.newDfp(0.1),
                                                              field.newDfp(singularEulerAngle[j]),
                                                              field.newDfp(0.3));
                try {
                    r.getAngles(EulerOrders[i]);
                    Assert.fail("an exception should have been caught");
                } catch (CardanEulerSingularityException cese) {
                    
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testQuaternion
    public void testQuaternion() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r1 = new FieldRotation<Dfp>(createVector(2, -3, 5), createAngle(1.7));
        double n = 23.5;
        FieldRotation<Dfp> r2 = new FieldRotation<Dfp>(r1.getQ0().multiply(n), r1.getQ1().multiply(n),
                                       r1.getQ2().multiply(n), r1.getQ3().multiply(n),
                                       true);
        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    checkVector(r2.applyTo(u), r1.applyTo(u));
                }
            }
        }

        r1 = createRotation(0.288,  0.384,  0.36,  0.8, false);
        checkRotationDS(r1,
                        -r1.getQ0().getReal(), -r1.getQ1().getReal(),
                        -r1.getQ2().getReal(), -r1.getQ3().getReal());

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testCompose
    public void testCompose() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r1       = new FieldRotation<Dfp>(createVector(2, -3, 5), createAngle(1.7));
        FieldRotation<Dfp> r2       = new FieldRotation<Dfp>(createVector(-1, 3, 2), createAngle(0.3));
        FieldRotation<Dfp> r3       = r2.applyTo(r1);
        FieldRotation<Dfp> r3Double = r2.applyTo(new Rotation(r1.getQ0().getReal(),
                                                      r1.getQ1().getReal(),
                                                      r1.getQ2().getReal(),
                                                      r1.getQ3().getReal(),
                                                      false));

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    checkVector(r2.applyTo(r1.applyTo(u)), r3.applyTo(u));
                    checkVector(r2.applyTo(r1.applyTo(u)), r3Double.applyTo(u));
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testComposeInverse
    public void testComposeInverse() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r1 = new FieldRotation<Dfp>(createVector(2, -3, 5), createAngle(1.7));
        FieldRotation<Dfp> r2 = new FieldRotation<Dfp>(createVector(-1, 3, 2), createAngle(0.3));
        FieldRotation<Dfp> r3 = r2.applyInverseTo(r1);
        FieldRotation<Dfp> r3Double = r2.applyInverseTo(new Rotation(r1.getQ0().getReal(),
                                                             r1.getQ1().getReal(),
                                                             r1.getQ2().getReal(),
                                                             r1.getQ3().getReal(),
                                                             false));

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    checkVector(r2.applyInverseTo(r1.applyTo(u)), r3.applyTo(u));
                    checkVector(r2.applyInverseTo(r1.applyTo(u)), r3Double.applyTo(u));
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testDoubleVectors
    public void testDoubleVectors() throws MathIllegalArgumentException {

        Well1024a random = new Well1024a(0x180b41cfeeffaf67l);
        UnitSphereRandomVectorGenerator g = new UnitSphereRandomVectorGenerator(3, random);
        for (int i = 0; i < 10; ++i) {
            double[] unit = g.nextVector();
            FieldRotation<Dfp> r = new FieldRotation<Dfp>(createVector(unit[0], unit[1], unit[2]),
                                          createAngle(random.nextDouble()));

            for (double x = -0.9; x < 0.9; x += 0.4) {
                for (double y = -0.9; y < 0.9; y += 0.4) {
                    for (double z = -0.9; z < 0.9; z += 0.4) {
                        FieldVector3D<Dfp> uds   = createVector(x, y, z);
                        FieldVector3D<Dfp> ruds  = r.applyTo(uds);
                        FieldVector3D<Dfp> rIuds = r.applyInverseTo(uds);
                        Vector3D   u     = new Vector3D(x, y, z);
                        FieldVector3D<Dfp> ru    = r.applyTo(u);
                        FieldVector3D<Dfp> rIu   = r.applyInverseTo(u);
                        Dfp[] ruArray = new Dfp[3];
                        r.applyTo(new double[] { x, y, z}, ruArray);
                        Dfp[] rIuArray = new Dfp[3];
                        r.applyInverseTo(new double[] { x, y, z}, rIuArray);
                        checkVector(ruds, ru);
                        checkVector(ruds, new FieldVector3D<Dfp>(ruArray));
                        checkVector(rIuds, rIu);
                        checkVector(rIuds, new FieldVector3D<Dfp>(rIuArray));
                    }
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testDoubleRotations
    public void testDoubleRotations() throws MathIllegalArgumentException {

        DfpField field = new DfpField(20);
        Well1024a random = new Well1024a(0x180b41cfeeffaf67l);
        UnitSphereRandomVectorGenerator g = new UnitSphereRandomVectorGenerator(3, random);
        for (int i = 0; i < 10; ++i) {
            double[] unit1 = g.nextVector();
            Rotation r1 = new Rotation(new Vector3D(unit1[0], unit1[1], unit1[2]),
                                      random.nextDouble());
            FieldRotation<Dfp> r1Prime = new FieldRotation<Dfp>(field.newDfp(r1.getQ0()),
                                                                field.newDfp(r1.getQ1()),
                                                                field.newDfp(r1.getQ2()),
                                                                field.newDfp(r1.getQ3()),
                                                                false);
            double[] unit2 = g.nextVector();
            FieldRotation<Dfp> r2 = new FieldRotation<Dfp>(createVector(unit2[0], unit2[1], unit2[2]),
                                           createAngle(random.nextDouble()));

            FieldRotation<Dfp> rA = FieldRotation.applyTo(r1, r2);
            FieldRotation<Dfp> rB = r1Prime.applyTo(r2);
            FieldRotation<Dfp> rC = FieldRotation.applyInverseTo(r1, r2);
            FieldRotation<Dfp> rD = r1Prime.applyInverseTo(r2);

            for (double x = -0.9; x < 0.9; x += 0.4) {
                for (double y = -0.9; y < 0.9; y += 0.4) {
                    for (double z = -0.9; z < 0.9; z += 0.4) {

                        FieldVector3D<Dfp> uds   = createVector(x, y, z);
                        checkVector(r1Prime.applyTo(uds), FieldRotation.applyTo(r1, uds));
                        checkVector(r1Prime.applyInverseTo(uds), FieldRotation.applyInverseTo(r1, uds));
                        checkVector(rA.applyTo(uds), rB.applyTo(uds));
                        checkVector(rA.applyInverseTo(uds), rB.applyInverseTo(uds));
                        checkVector(rC.applyTo(uds), rD.applyTo(uds));
                        checkVector(rC.applyInverseTo(uds), rD.applyInverseTo(uds));

                    }
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testArray
    public void testArray() throws MathIllegalArgumentException {

        FieldRotation<Dfp> r = new FieldRotation<Dfp>(createAxis(2, -3, 5), createAngle(1.7));

        for (double x = -0.9; x < 0.9; x += 0.2) {
            for (double y = -0.9; y < 0.9; y += 0.2) {
                for (double z = -0.9; z < 0.9; z += 0.2) {
                    FieldVector3D<Dfp> u = createVector(x, y, z);
                    FieldVector3D<Dfp> v = r.applyTo(u);
                    Dfp[] out = new Dfp[3];
                    r.applyTo(new Dfp[] { u.getX(), u.getY(), u.getZ() }, out);
                    Assert.assertEquals(v.getX().getReal(), out[0].getReal(), 1.0e-10);
                    Assert.assertEquals(v.getY().getReal(), out[1].getReal(), 1.0e-10);
                    Assert.assertEquals(v.getZ().getReal(), out[2].getReal(), 1.0e-10);
                    r.applyInverseTo(out, out);
                    Assert.assertEquals(u.getX().getReal(), out[0].getReal(), 1.0e-10);
                    Assert.assertEquals(u.getY().getReal(), out[1].getReal(), 1.0e-10);
                    Assert.assertEquals(u.getZ().getReal(), out[2].getReal(), 1.0e-10);
                }
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testApplyInverseTo
    public void testApplyInverseTo() throws MathIllegalArgumentException {

        Dfp[] in      = new Dfp[3];
        Dfp[] out     = new Dfp[3];
        Dfp[] rebuilt = new Dfp[3];
        FieldRotation<Dfp> r = new FieldRotation<Dfp>(createVector(2, -3, 5), createAngle(1.7));
        for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
            for (double phi = -1.55; phi < 1.55; phi += 0.2) {
                FieldVector3D<Dfp> u = createVector(FastMath.cos(lambda) * FastMath.cos(phi),
                                          FastMath.sin(lambda) * FastMath.cos(phi),
                                          FastMath.sin(phi));
                r.applyInverseTo(r.applyTo(u));
                checkVector(u, r.applyInverseTo(r.applyTo(u)));
                checkVector(u, r.applyTo(r.applyInverseTo(u)));
                in[0] = u.getX();
                in[1] = u.getY();
                in[2] = u.getZ();
                r.applyTo(in, out);
                r.applyInverseTo(out, rebuilt);
                Assert.assertEquals(in[0].getReal(), rebuilt[0].getReal(), 1.0e-12);
                Assert.assertEquals(in[1].getReal(), rebuilt[1].getReal(), 1.0e-12);
                Assert.assertEquals(in[2].getReal(), rebuilt[2].getReal(), 1.0e-12);
            }
        }

        r = createRotation(1, 0, 0, 0, false);
        for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
            for (double phi = -1.55; phi < 1.55; phi += 0.2) {
                FieldVector3D<Dfp> u = createVector(FastMath.cos(lambda) * FastMath.cos(phi),
                                          FastMath.sin(lambda) * FastMath.cos(phi),
                                          FastMath.sin(phi));
                checkVector(u, r.applyInverseTo(r.applyTo(u)));
                checkVector(u, r.applyTo(r.applyInverseTo(u)));
            }
        }

        r = new FieldRotation<Dfp>(createVector(0, 0, 1), createAngle(FastMath.PI));
        for (double lambda = 0; lambda < 6.2; lambda += 0.2) {
            for (double phi = -1.55; phi < 1.55; phi += 0.2) {
                FieldVector3D<Dfp> u = createVector(FastMath.cos(lambda) * FastMath.cos(phi),
                                          FastMath.sin(lambda) * FastMath.cos(phi),
                                          FastMath.sin(phi));
                checkVector(u, r.applyInverseTo(r.applyTo(u)));
                checkVector(u, r.applyTo(r.applyInverseTo(u)));
            }
        }

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testIssue639
    public void testIssue639() throws MathArithmeticException{
        FieldVector3D<Dfp> u1 = createVector(-1321008684645961.0 /  268435456.0,
                                   -5774608829631843.0 /  268435456.0,
                                   -3822921525525679.0 / 4294967296.0);
        FieldVector3D<Dfp> u2 =createVector( -5712344449280879.0 /    2097152.0,
                                   -2275058564560979.0 /    1048576.0,
                                   4423475992255071.0 /      65536.0);
        FieldRotation<Dfp> rot = new FieldRotation<Dfp>(u1, u2, createVector(1, 0, 0),createVector(0, 0, 1));
        Assert.assertEquals( 0.6228370359608200639829222, rot.getQ0().getReal(), 1.0e-15);
        Assert.assertEquals( 0.0257707621456498790029987, rot.getQ1().getReal(), 1.0e-15);
        Assert.assertEquals(-0.0000000002503012255839931, rot.getQ2().getReal(), 1.0e-15);
        Assert.assertEquals(-0.7819270390861109450724902, rot.getQ3().getReal(), 1.0e-15);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldRotationDfpTest::testIssue801
    public void testIssue801() throws MathArithmeticException {
        FieldVector3D<Dfp> u1 = createVector(0.9999988431610581, -0.0015210774290851095, 0.0);
        FieldVector3D<Dfp> u2 = createVector(0.0, 0.0, 1.0);

        FieldVector3D<Dfp> v1 = createVector(0.9999999999999999, 0.0, 0.0);
        FieldVector3D<Dfp> v2 = createVector(0.0, 0.0, -1.0);

        FieldRotation<Dfp> quat = new FieldRotation<Dfp>(u1, u2, v1, v2);
        double q2 = quat.getQ0().getReal() * quat.getQ0().getReal() +
                    quat.getQ1().getReal() * quat.getQ1().getReal() +
                    quat.getQ2().getReal() * quat.getQ2().getReal() +
                    quat.getQ3().getReal() * quat.getQ3().getReal();
        Assert.assertEquals(1.0, q2, 1.0e-14);
        Assert.assertEquals(0.0, FieldVector3D.angle(v1, quat.applyTo(u1)).getReal(), 1.0e-14);
        Assert.assertEquals(0.0, FieldVector3D.angle(v2, quat.applyTo(u2)).getReal(), 1.0e-14);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testConstructors
    public void testConstructors() throws DimensionMismatchException {
        double cosAlpha = 1 / 2.0;
        double sinAlpha = FastMath.sqrt(3) / 2.0;
        double cosDelta = FastMath.sqrt(2) / 2.0;
        double sinDelta = -FastMath.sqrt(2) / 2.0;
        FieldVector3D<DerivativeStructure> u = new FieldVector3D<DerivativeStructure>(2,
                new FieldVector3D<DerivativeStructure>(new DerivativeStructure(2, 1, 0,  FastMath.PI / 3),
                        new DerivativeStructure(2, 1, 1, -FastMath.PI / 4)));
        checkVector(u, 2 * cosAlpha * cosDelta, 2 * sinAlpha * cosDelta, 2 * sinDelta);
        Assert.assertEquals(-2 * sinAlpha * cosDelta, u.getX().getPartialDerivative(1, 0), 1.0e-12);
        Assert.assertEquals(+2 * cosAlpha * cosDelta, u.getY().getPartialDerivative(1, 0), 1.0e-12);
        Assert.assertEquals(0,                        u.getZ().getPartialDerivative(1, 0), 1.0e-12);
        Assert.assertEquals(-2 * cosAlpha * sinDelta, u.getX().getPartialDerivative(0, 1), 1.0e-12);
        Assert.assertEquals(-2 * sinAlpha * sinDelta, u.getY().getPartialDerivative(0, 1), 1.0e-12);
        Assert.assertEquals(2 * cosDelta,             u.getZ().getPartialDerivative(0, 1), 1.0e-12);

        checkVector(new FieldVector3D<DerivativeStructure>(2, createVector(1, 0,  0, 3)),
                                   2, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 2);
        checkVector(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(4, 1, 3,  2.0),
                                   createVector(1, 0,  0, 4)),
                                   2, 0, 0, 2, 0, 0, 1, 0, 2, 0, 0, 0, 0, 2, 0);
        checkVector(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(4, 1, 3,  2.0),
                                   new Vector3D(1, 0,  0)),
                                   2, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);

        checkVector(new FieldVector3D<DerivativeStructure>(2, createVector(1, 0,  0, 3),
                                   -3, createVector(0, 0, -1, 3)),
                                   2, 0, 3, -1, 0, 0, 0, -1, 0, 0, 0, -1);
        checkVector(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(4, 1, 3,  2.0),
                                   createVector(1, 0,  0, 4),
                                   new DerivativeStructure(4, 1, 3, -3.0),
                                   createVector(0, 0, -1, 4)),
                                   2, 0, 3, -1, 0, 0, 1, 0, -1, 0, 0, 0, 0, -1, -1);
        checkVector(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(4, 1, 3,  2.0),
                                   new Vector3D(1, 0,  0),
                                   new DerivativeStructure(4, 1, 3, -3.0),
                                   new Vector3D(0, 0, -1)),
                                   2, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -1);

        checkVector(new FieldVector3D<DerivativeStructure>(2, createVector(1, 0, 0, 3),
                                   5, createVector(0, 1, 0, 3),
                                   -3, createVector(0, 0, -1, 3)),
                                   2, 5, 3, 4, 0, 0, 0, 4, 0, 0, 0, 4);
        checkVector(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(4, 1, 3,  2.0),
                                   createVector(1, 0,  0, 4),
                                   new DerivativeStructure(4, 1, 3,  5.0),
                                   createVector(0, 1,  0, 4),
                                   new DerivativeStructure(4, 1, 3, -3.0),
                                   createVector(0, 0, -1, 4)),
                                   2, 5, 3, 4, 0, 0, 1, 0, 4, 0, 1, 0, 0, 4, -1);
        checkVector(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(4, 1, 3,  2.0),
                                   new Vector3D(1, 0,  0),
                                   new DerivativeStructure(4, 1, 3,  5.0),
                                   new Vector3D(0, 1,  0),
                                   new DerivativeStructure(4, 1, 3, -3.0),
                                   new Vector3D(0, 0, -1)),
                                   2, 5, 3, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, -1);

        checkVector(new FieldVector3D<DerivativeStructure>(2, createVector(1, 0, 0, 3),
                                   5, createVector(0, 1, 0, 3),
                                   5, createVector(0, -1, 0, 3),
                                   -3, createVector(0, 0, -1, 3)),
                                   2, 0, 3, 9, 0, 0, 0, 9, 0, 0, 0, 9);
        checkVector(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(4, 1, 3,  2.0),
                                   createVector(1, 0,  0, 4),
                                   new DerivativeStructure(4, 1, 3,  5.0),
                                   createVector(0, 1,  0, 4),
                                   new DerivativeStructure(4, 1, 3,  5.0),
                                   createVector(0, -1,  0, 4),
                                   new DerivativeStructure(4, 1, 3, -3.0),
                                   createVector(0, 0, -1, 4)),
                                   2, 0, 3, 9, 0, 0, 1, 0, 9, 0, 0, 0, 0, 9, -1);
        checkVector(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(4, 1, 3,  2.0),
                                   new Vector3D(1, 0,  0),
                                   new DerivativeStructure(4, 1, 3,  5.0),
                                   new Vector3D(0, 1,  0),
                                   new DerivativeStructure(4, 1, 3,  5.0),
                                   new Vector3D(0, -1,  0),
                                   new DerivativeStructure(4, 1, 3, -3.0),
                                   new Vector3D(0, 0, -1)),
                                   2, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, -1);

        checkVector(new FieldVector3D<DerivativeStructure>(new DerivativeStructure[] {
            new DerivativeStructure(3, 1, 2,  2),
            new DerivativeStructure(3, 1, 1,  5),
            new DerivativeStructure(3, 1, 0, -3)
        }),
        2, 5, -3, 0, 0, 1, 0, 1, 0, 1, 0, 0);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testEquals
    public void testEquals() {
        FieldVector3D<DerivativeStructure> u1 = createVector(1, 2, 3, 3);
        FieldVector3D<DerivativeStructure> v  = createVector(1, 2, 3 + 10 * Precision.EPSILON, 3);
        Assert.assertTrue(u1.equals(u1));
        Assert.assertTrue(u1.equals(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(3, 1, 0, 1.0),
                                                   new DerivativeStructure(3, 1, 1, 2.0),
                                                   new DerivativeStructure(3, 1, 2, 3.0))));
        Assert.assertFalse(u1.equals(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(3, 1, 1.0),
                                                    new DerivativeStructure(3, 1, 1, 2.0),
                                                    new DerivativeStructure(3, 1, 2, 3.0))));
        Assert.assertFalse(u1.equals(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(3, 1, 0, 1.0),
                                                    new DerivativeStructure(3, 1, 2.0),
                                                    new DerivativeStructure(3, 1, 2, 3.0))));
        Assert.assertFalse(u1.equals(new FieldVector3D<DerivativeStructure>(new DerivativeStructure(3, 1, 0, 1.0),
                                                    new DerivativeStructure(3, 1, 1, 2.0),
                                                    new DerivativeStructure(3, 1, 3.0))));
        Assert.assertFalse(u1.equals(v));
        Assert.assertFalse(u1.equals(u1.toVector3D()));
        Assert.assertTrue(createVector(0, Double.NaN, 0, 3).equals(createVector(0, 0, Double.NaN, 3)));
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testHash
    public void testHash() {
        Assert.assertEquals(createVector(0, Double.NaN, 0, 3).hashCode(), createVector(0, 0, Double.NaN, 3).hashCode());
        FieldVector3D<DerivativeStructure> u = createVector(1, 2, 3, 3);
        FieldVector3D<DerivativeStructure> v = createVector(1, 2, 3 + 10 * Precision.EPSILON, 3);
        Assert.assertTrue(u.hashCode() != v.hashCode());
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testInfinite
    public void testInfinite() {
        Assert.assertTrue(createVector(1, 1, Double.NEGATIVE_INFINITY, 3).isInfinite());
        Assert.assertTrue(createVector(1, Double.NEGATIVE_INFINITY, 1, 3).isInfinite());
        Assert.assertTrue(createVector(Double.NEGATIVE_INFINITY, 1, 1, 3).isInfinite());
        Assert.assertFalse(createVector(1, 1, 2, 3).isInfinite());
        Assert.assertFalse(createVector(1, Double.NaN, Double.NEGATIVE_INFINITY, 3).isInfinite());
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testNaN
    public void testNaN() {
        Assert.assertTrue(createVector(1, 1, Double.NaN, 3).isNaN());
        Assert.assertTrue(createVector(1, Double.NaN, 1, 3).isNaN());
        Assert.assertTrue(createVector(Double.NaN, 1, 1, 3).isNaN());
        Assert.assertFalse(createVector(1, 1, 2, 3).isNaN());
        Assert.assertFalse(createVector(1, 1, Double.NEGATIVE_INFINITY, 3).isNaN());
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testToString
    public void testToString() {
        Assert.assertEquals("{3; 2; 1}", createVector(3, 2, 1, 3).toString());
        NumberFormat format = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));
        Assert.assertEquals("{3.000; 2.000; 1.000}", createVector(3, 2, 1, 3).toString(format));
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testWrongDimension
    public void testWrongDimension() throws DimensionMismatchException {
        new FieldVector3D<DerivativeStructure>(new DerivativeStructure[] {
            new DerivativeStructure(3, 1, 0, 2),
            new DerivativeStructure(3, 1, 0, 5)
        });
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testCoordinates
    public void testCoordinates() {
        FieldVector3D<DerivativeStructure> v = createVector(1, 2, 3, 3);
        Assert.assertTrue(FastMath.abs(v.getX().getReal() - 1) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(v.getY().getReal() - 2) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(v.getZ().getReal() - 3) < 1.0e-12);
        DerivativeStructure[] coordinates = v.toArray();
        Assert.assertTrue(FastMath.abs(coordinates[0].getReal() - 1) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(coordinates[1].getReal() - 2) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(coordinates[2].getReal() - 3) < 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testNorm1
    public void testNorm1() {
        Assert.assertEquals( 0.0, createVector(0, 0, 0, 3).getNorm1().getReal(), 0);
        Assert.assertEquals( 6.0, createVector(1, -2, 3, 3).getNorm1().getReal(), 0);
        Assert.assertEquals( 1.0, createVector(1, -2, 3, 3).getNorm1().getPartialDerivative(1, 0, 0), 0);
        Assert.assertEquals(-1.0, createVector(1, -2, 3, 3).getNorm1().getPartialDerivative(0, 1, 0), 0);
        Assert.assertEquals( 1.0, createVector(1, -2, 3, 3).getNorm1().getPartialDerivative(0, 0, 1), 0);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testNorm
    public void testNorm() {
        double r = FastMath.sqrt(14);
        Assert.assertEquals(0.0, createVector(0, 0, 0, 3).getNorm().getReal(), 0);
        Assert.assertEquals(r, createVector(1, 2, 3, 3).getNorm().getReal(), 1.0e-12);
        Assert.assertEquals( 1.0 / r, createVector(1, 2, 3, 3).getNorm().getPartialDerivative(1, 0, 0), 0);
        Assert.assertEquals( 2.0 / r, createVector(1, 2, 3, 3).getNorm().getPartialDerivative(0, 1, 0), 0);
        Assert.assertEquals( 3.0 / r, createVector(1, 2, 3, 3).getNorm().getPartialDerivative(0, 0, 1), 0);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testNormSq
    public void testNormSq() {
        Assert.assertEquals(0.0, createVector(0, 0, 0, 3).getNormSq().getReal(), 0);
        Assert.assertEquals(14, createVector(1, 2, 3, 3).getNormSq().getReal(), 1.0e-12);
        Assert.assertEquals( 2, createVector(1, 2, 3, 3).getNormSq().getPartialDerivative(1, 0, 0), 0);
        Assert.assertEquals( 4, createVector(1, 2, 3, 3).getNormSq().getPartialDerivative(0, 1, 0), 0);
        Assert.assertEquals( 6, createVector(1, 2, 3, 3).getNormSq().getPartialDerivative(0, 0, 1), 0);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testNormInf
    public void testNormInf() {
        Assert.assertEquals( 0.0, createVector(0, 0, 0, 3).getNormInf().getReal(), 0);
        Assert.assertEquals( 3.0, createVector(1, -2, 3, 3).getNormInf().getReal(), 0);
        Assert.assertEquals( 0.0, createVector(1, -2, 3, 3).getNormInf().getPartialDerivative(1, 0, 0), 0);
        Assert.assertEquals( 0.0, createVector(1, -2, 3, 3).getNormInf().getPartialDerivative(0, 1, 0), 0);
        Assert.assertEquals( 1.0, createVector(1, -2, 3, 3).getNormInf().getPartialDerivative(0, 0, 1), 0);
        Assert.assertEquals( 3.0, createVector(2, -1, 3, 3).getNormInf().getReal(), 0);
        Assert.assertEquals( 0.0, createVector(2, -1, 3, 3).getNormInf().getPartialDerivative(1, 0, 0), 0);
        Assert.assertEquals( 0.0, createVector(2, -1, 3, 3).getNormInf().getPartialDerivative(0, 1, 0), 0);
        Assert.assertEquals( 1.0, createVector(2, -1, 3, 3).getNormInf().getPartialDerivative(0, 0, 1), 0);
        Assert.assertEquals( 3.0, createVector(1, -3, 2, 3).getNormInf().getReal(), 0);
        Assert.assertEquals( 0.0, createVector(1, -3, 2, 3).getNormInf().getPartialDerivative(1, 0, 0), 0);
        Assert.assertEquals(-1.0, createVector(1, -3, 2, 3).getNormInf().getPartialDerivative(0, 1, 0), 0);
        Assert.assertEquals( 0.0, createVector(1, -3, 2, 3).getNormInf().getPartialDerivative(0, 0, 1), 0);
        Assert.assertEquals( 3.0, createVector(2, -3, 1, 3).getNormInf().getReal(), 0);
        Assert.assertEquals( 0.0, createVector(2, -3, 1, 3).getNormInf().getPartialDerivative(1, 0, 0), 0);
        Assert.assertEquals(-1.0, createVector(2, -3, 1, 3).getNormInf().getPartialDerivative(0, 1, 0), 0);
        Assert.assertEquals( 0.0, createVector(2, -3, 1, 3).getNormInf().getPartialDerivative(0, 0, 1), 0);
        Assert.assertEquals( 3.0, createVector(3, -1, 2, 3).getNormInf().getReal(), 0);
        Assert.assertEquals( 1.0, createVector(3, -1, 2, 3).getNormInf().getPartialDerivative(1, 0, 0), 0);
        Assert.assertEquals( 0.0, createVector(3, -1, 2, 3).getNormInf().getPartialDerivative(0, 1, 0), 0);
        Assert.assertEquals( 0.0, createVector(3, -1, 2, 3).getNormInf().getPartialDerivative(0, 0, 1), 0);
        Assert.assertEquals( 3.0, createVector(3, -2, 1, 3).getNormInf().getReal(), 0);
        Assert.assertEquals( 1.0, createVector(3, -2, 1, 3).getNormInf().getPartialDerivative(1, 0, 0), 0);
        Assert.assertEquals( 0.0, createVector(3, -2, 1, 3).getNormInf().getPartialDerivative(0, 1, 0), 0);
        Assert.assertEquals( 0.0, createVector(3, -2, 1, 3).getNormInf().getPartialDerivative(0, 0, 1), 0);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testDistance1
    public void testDistance1() {
        FieldVector3D<DerivativeStructure> v1 = createVector(1, -2, 3, 3);
        FieldVector3D<DerivativeStructure> v2 = createVector(-4, 2, 0, 3);
        Assert.assertEquals(0.0, FieldVector3D.distance1(createVector(-1, 0, 0, 3), createVector(-1, 0, 0, 3)).getReal(), 0);
        DerivativeStructure distance = FieldVector3D.distance1(v1, v2);
        Assert.assertEquals(12.0, distance.getReal(), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(0, 0, 1), 1.0e-12);
        distance = FieldVector3D.distance1(v1, new Vector3D(-4, 2, 0));
        Assert.assertEquals(12.0, distance.getReal(), 1.0e-12);
        Assert.assertEquals( 1, distance.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(-1, distance.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals( 1, distance.getPartialDerivative(0, 0, 1), 1.0e-12);
        distance = FieldVector3D.distance1(new Vector3D(-4, 2, 0), v1);
        Assert.assertEquals(12.0, distance.getReal(), 1.0e-12);
        Assert.assertEquals( 1, distance.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(-1, distance.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals( 1, distance.getPartialDerivative(0, 0, 1), 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testDistance
    public void testDistance() {
        FieldVector3D<DerivativeStructure> v1 = createVector(1, -2, 3, 3);
        FieldVector3D<DerivativeStructure> v2 = createVector(-4, 2, 0, 3);
        Assert.assertEquals(0.0, FieldVector3D.distance(createVector(-1, 0, 0, 3), createVector(-1, 0, 0, 3)).getReal(), 0);
        DerivativeStructure distance = FieldVector3D.distance(v1, v2);
        Assert.assertEquals(FastMath.sqrt(50), distance.getReal(), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(0, 0, 1), 1.0e-12);
        distance = FieldVector3D.distance(v1, new Vector3D(-4, 2, 0));
        Assert.assertEquals(FastMath.sqrt(50), distance.getReal(), 1.0e-12);
        Assert.assertEquals( 5 / FastMath.sqrt(50), distance.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(-4 / FastMath.sqrt(50), distance.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals( 3 / FastMath.sqrt(50), distance.getPartialDerivative(0, 0, 1), 1.0e-12);
        distance = FieldVector3D.distance(new Vector3D(-4, 2, 0), v1);
        Assert.assertEquals(FastMath.sqrt(50), distance.getReal(), 1.0e-12);
        Assert.assertEquals( 5 / FastMath.sqrt(50), distance.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(-4 / FastMath.sqrt(50), distance.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals( 3 / FastMath.sqrt(50), distance.getPartialDerivative(0, 0, 1), 1.0e-12);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testDistanceSq
    public void testDistanceSq() {
        FieldVector3D<DerivativeStructure> v1 = createVector(1, -2, 3, 3);
        FieldVector3D<DerivativeStructure> v2 = createVector(-4, 2, 0, 3);
        Assert.assertEquals(0.0, FieldVector3D.distanceSq(createVector(-1, 0, 0, 3), createVector(-1, 0, 0, 3)).getReal(), 0);
        DerivativeStructure distanceSq = FieldVector3D.distanceSq(v1, v2);
        Assert.assertEquals(50.0, distanceSq.getReal(), 1.0e-12);
        Assert.assertEquals(0, distanceSq.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(0, distanceSq.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals(0, distanceSq.getPartialDerivative(0, 0, 1), 1.0e-12);
        distanceSq = FieldVector3D.distanceSq(v1, new Vector3D(-4, 2, 0));
        Assert.assertEquals(50.0, distanceSq.getReal(), 1.0e-12);
        Assert.assertEquals(10, distanceSq.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(-8, distanceSq.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals( 6, distanceSq.getPartialDerivative(0, 0, 1), 1.0e-12);
        distanceSq = FieldVector3D.distanceSq(new Vector3D(-4, 2, 0), v1);
        Assert.assertEquals(50.0, distanceSq.getReal(), 1.0e-12);
        Assert.assertEquals(10, distanceSq.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(-8, distanceSq.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals( 6, distanceSq.getPartialDerivative(0, 0, 1), 1.0e-12);
  }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testDistanceInf
    public void testDistanceInf() {
        FieldVector3D<DerivativeStructure> v1 = createVector(1, -2, 3, 3);
        FieldVector3D<DerivativeStructure> v2 = createVector(-4, 2, 0, 3);
        Assert.assertEquals(0.0, FieldVector3D.distanceInf(createVector(-1, 0, 0, 3), createVector(-1, 0, 0, 3)).getReal(), 0);
        DerivativeStructure distance = FieldVector3D.distanceInf(v1, v2);
        Assert.assertEquals(5.0, distance.getReal(), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(0, 0, 1), 1.0e-12);
        distance = FieldVector3D.distanceInf(v1, new Vector3D(-4, 2, 0));
        Assert.assertEquals(5.0, distance.getReal(), 1.0e-12);
        Assert.assertEquals(1, distance.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(0, 0, 1), 1.0e-12);
        distance = FieldVector3D.distanceInf(new Vector3D(-4, 2, 0), v1);
        Assert.assertEquals(5.0, distance.getReal(), 1.0e-12);
        Assert.assertEquals(1, distance.getPartialDerivative(1, 0, 0), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(0, 1, 0), 1.0e-12);
        Assert.assertEquals(0, distance.getPartialDerivative(0, 0, 1), 1.0e-12);
        Assert.assertEquals(v1.subtract(v2).getNormInf().getReal(), FieldVector3D.distanceInf(v1, v2).getReal(), 1.0e-12);

        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector( 1, -2, 3, 3), createVector(-4,  2, 0, 3)).getReal(),
                            1.0e-12);
        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector( 1, 3, -2, 3), createVector(-4, 0,  2, 3)).getReal(),
                            1.0e-12);
        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector(-2,  1, 3, 3), createVector( 2, -4, 0, 3)).getReal(),
                            1.0e-12);
        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector(-2, 3,  1, 3), createVector( 2, 0, -4, 3)).getReal(),
                            1.0e-12);
        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector(3, -2,  1, 3), createVector(0,  2, -4, 3)).getReal(),
                            1.0e-12);
        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector(3,  1, -2, 3), createVector(0, -4,  2, 3)).getReal(),
                            1.0e-12);

        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector( 1, -2, 3, 3), new Vector3D(-4,  2, 0)).getReal(),
                            1.0e-12);
        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector( 1, 3, -2, 3), new Vector3D(-4, 0,  2)).getReal(),
                            1.0e-12);
        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector(-2,  1, 3, 3), new Vector3D( 2, -4, 0)).getReal(),
                            1.0e-12);
        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector(-2, 3,  1, 3), new Vector3D( 2, 0, -4)).getReal(),
                            1.0e-12);
        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector(3, -2,  1, 3), new Vector3D(0,  2, -4)).getReal(),
                            1.0e-12);
        Assert.assertEquals(5.0,
                            FieldVector3D.distanceInf(createVector(3,  1, -2, 3), new Vector3D(0, -4,  2)).getReal(),
                            1.0e-12);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testSubtract
    public void testSubtract() {
        FieldVector3D<DerivativeStructure> v1 = createVector(1, 2, 3, 3);
        FieldVector3D<DerivativeStructure> v2 = createVector(-3, -2, -1, 3);
        v1 = v1.subtract(v2);
        checkVector(v1, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        checkVector(v2.subtract(v1), -7, -6, -5, 1, 0, 0, 0, 1, 0, 0, 0, 1);
        checkVector(v2.subtract(new Vector3D(4, 4, 4)), -7, -6, -5, 1, 0, 0, 0, 1, 0, 0, 0, 1);
        checkVector(v2.subtract(3, v1), -15, -14, -13, 1, 0, 0, 0, 1, 0, 0, 0, 1);
        checkVector(v2.subtract(3, new Vector3D(4, 4, 4)), -15, -14, -13, 1, 0, 0, 0, 1, 0, 0, 0, 1);
        checkVector(v2.subtract(new DerivativeStructure(3, 1, 2, 3), new Vector3D(4, 4, 4)),
                    -15, -14, -13, 1, 0, -4, 0, 1, -4, 0, 0, -3);

        checkVector(createVector(1, 2, 3, 4).subtract(new DerivativeStructure(4, 1, 3, 5.0),
                                                      createVector(3, -2, 1, 4)),
                    -14, 12, -2,
                     -4,  0,  0, -3,
                      0, -4,  0,  2,
                      0,  0, -4, -1);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testAdd
    public void testAdd() {
        FieldVector3D<DerivativeStructure> v1 = createVector(1, 2, 3, 3);
        FieldVector3D<DerivativeStructure> v2 = createVector(-3, -2, -1, 3);
        v1 = v1.add(v2);
        checkVector(v1, -2, 0, 2, 2, 0, 0, 0, 2, 0, 0, 0, 2);

        checkVector(v2.add(v1), -5, -2, 1, 3, 0, 0, 0, 3, 0, 0, 0, 3);
        checkVector(v2.add(new Vector3D(-2, 0, 2)), -5, -2, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1);
        checkVector(v2.add(3, v1), -9, -2, 5, 7, 0, 0, 0, 7, 0, 0, 0, 7);
        checkVector(v2.add(3, new Vector3D(-2, 0, 2)), -9, -2, 5, 1, 0, 0, 0, 1, 0, 0, 0, 1);
        checkVector(v2.add(new DerivativeStructure(3, 1, 2, 3), new Vector3D(-2, 0, 2)),
                    -9, -2, 5, 1, 0, -2, 0, 1, 0, 0, 0, 3);

        checkVector(createVector(1, 2, 3, 4).add(new DerivativeStructure(4, 1, 3, 5.0),
                                                 createVector(3, -2, 1, 4)),
                    16, -8,  8,
                     6,  0,  0,  3,
                     0,  6,  0, -2,
                     0,  0,  6,  1);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testScalarProduct
    public void testScalarProduct() {
        FieldVector3D<DerivativeStructure> v = createVector(1, 2, 3, 3);
        v = v.scalarMultiply(3);
        checkVector(v, 3, 6, 9);

        checkVector(v.scalarMultiply(0.5), 1.5, 3, 4.5);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testVectorialProducts
    public void testVectorialProducts() {
        FieldVector3D<DerivativeStructure> v1 = createVector(2, 1, -4, 3);
        FieldVector3D<DerivativeStructure> v2 = createVector(3, 1, -1, 3);

        Assert.assertTrue(FastMath.abs(FieldVector3D.dotProduct(v1, v2).getReal() - 11) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(FieldVector3D.dotProduct(v1, v2.toVector3D()).getReal() - 11) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(FieldVector3D.dotProduct(v1.toVector3D(), v2).getReal() - 11) < 1.0e-12);

        FieldVector3D<DerivativeStructure> v3 = FieldVector3D.crossProduct(v1, v2);
        checkVector(v3, 3, -10, -1);
        Assert.assertTrue(FastMath.abs(FieldVector3D.dotProduct(v1, v3).getReal()) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(FieldVector3D.dotProduct(v2, v3).getReal()) < 1.0e-12);

        v3 = FieldVector3D.crossProduct(v1, v2.toVector3D());
        checkVector(v3, 3, -10, -1);
        Assert.assertTrue(FastMath.abs(FieldVector3D.dotProduct(v1, v3).getReal()) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(FieldVector3D.dotProduct(v2, v3).getReal()) < 1.0e-12);

        v3 = FieldVector3D.crossProduct(v1.toVector3D(), v2);
        checkVector(v3, 3, -10, -1);
        Assert.assertTrue(FastMath.abs(FieldVector3D.dotProduct(v1, v3).getReal()) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(FieldVector3D.dotProduct(v2, v3).getReal()) < 1.0e-12);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testCrossProductCancellation
    public void testCrossProductCancellation() {
        FieldVector3D<DerivativeStructure> v1 = createVector(9070467121.0, 4535233560.0, 1, 3);
        FieldVector3D<DerivativeStructure> v2 = createVector(9070467123.0, 4535233561.0, 1, 3);
        checkVector(FieldVector3D.crossProduct(v1, v2), -1, 2, 1);

        double scale    = FastMath.scalb(1.0, 100);
        FieldVector3D<DerivativeStructure> big1   = new FieldVector3D<DerivativeStructure>(scale, v1);
        FieldVector3D<DerivativeStructure> small2 = new FieldVector3D<DerivativeStructure>(1 / scale, v2);
        checkVector(FieldVector3D.crossProduct(big1, small2), -1, 2, 1);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testAngular
    public void testAngular() {
        Assert.assertEquals(0,           createVector(1, 0, 0, 3).getAlpha().getReal(), 1.0e-10);
        Assert.assertEquals(0,           createVector(1, 0, 0, 3).getDelta().getReal(), 1.0e-10);
        Assert.assertEquals(FastMath.PI / 2, createVector(0, 1, 0, 3).getAlpha().getReal(), 1.0e-10);
        Assert.assertEquals(0,           createVector(0, 1, 0, 3).getDelta().getReal(), 1.0e-10);
        Assert.assertEquals(FastMath.PI / 2, createVector(0, 0, 1, 3).getDelta().getReal(), 1.0e-10);
      
        FieldVector3D<DerivativeStructure> u = createVector(-1, 1, -1, 3);
        Assert.assertEquals(3 * FastMath.PI /4, u.getAlpha().getReal(), 1.0e-10);
        Assert.assertEquals(-1.0 / FastMath.sqrt(3), u.getDelta().sin().getReal(), 1.0e-10);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testAngularSeparation
    public void testAngularSeparation() throws MathArithmeticException {
        FieldVector3D<DerivativeStructure> v1 = createVector(2, -1, 4, 3);

        FieldVector3D<DerivativeStructure>  k = v1.normalize();
        FieldVector3D<DerivativeStructure>  i = k.orthogonal();
        FieldVector3D<DerivativeStructure> v2 = k.scalarMultiply(FastMath.cos(1.2)).add(i.scalarMultiply(FastMath.sin(1.2)));

        Assert.assertTrue(FastMath.abs(FieldVector3D.angle(v1, v2).getReal() - 1.2) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(FieldVector3D.angle(v1, v2.toVector3D()).getReal() - 1.2) < 1.0e-12);
        Assert.assertTrue(FastMath.abs(FieldVector3D.angle(v1.toVector3D(), v2).getReal() - 1.2) < 1.0e-12);

        try {
            FieldVector3D.angle(v1, Vector3D.ZERO);
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException mae) {
            
        }
        Assert.assertEquals(0.0, FieldVector3D.angle(v1, v1.toVector3D()).getReal(), 1.0e-15);
        Assert.assertEquals(FastMath.PI, FieldVector3D.angle(v1, v1.negate().toVector3D()).getReal(), 1.0e-15);

    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testNormalize
    public void testNormalize() throws MathArithmeticException {
        Assert.assertEquals(1.0, createVector(5, -4, 2, 3).normalize().getNorm().getReal(), 1.0e-12);
        try {
            createVector(0, 0, 0, 3).normalize();
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException ae) {
            
        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testNegate
    public void testNegate() {
        checkVector(createVector(0.1, 2.5, 1.3, 3).negate(),
                    -0.1, -2.5, -1.3, -1, 0, 0, 0, -1, 0, 0, 0, -1);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testOrthogonal
    public void testOrthogonal() throws MathArithmeticException {
        FieldVector3D<DerivativeStructure> v1 = createVector(0.1, 2.5, 1.3, 3);
        Assert.assertEquals(0.0, FieldVector3D.dotProduct(v1, v1.orthogonal()).getReal(), 1.0e-12);
        FieldVector3D<DerivativeStructure> v2 = createVector(2.3, -0.003, 7.6, 3);
        Assert.assertEquals(0.0, FieldVector3D.dotProduct(v2, v2.orthogonal()).getReal(), 1.0e-12);
        FieldVector3D<DerivativeStructure> v3 = createVector(-1.7, 1.4, 0.2, 3);
        Assert.assertEquals(0.0, FieldVector3D.dotProduct(v3, v3.orthogonal()).getReal(), 1.0e-12);
        FieldVector3D<DerivativeStructure> v4 = createVector(4.2, 0.1, -1.8, 3);
        Assert.assertEquals(0.0, FieldVector3D.dotProduct(v4, v4.orthogonal()).getReal(), 1.0e-12);
        try {
            createVector(0, 0, 0, 3).orthogonal();
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException ae) {
            
        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testAngle
    public void testAngle() throws MathArithmeticException {
        Assert.assertEquals(0.22572612855273393616,
                            FieldVector3D.angle(createVector(1, 2, 3, 3), createVector(4, 5, 6, 3)).getReal(),
                            1.0e-12);
        Assert.assertEquals(7.98595620686106654517199e-8,
                            FieldVector3D.angle(createVector(1, 2, 3, 3), createVector(2, 4, 6.000001, 3)).getReal(),
                            1.0e-12);
        Assert.assertEquals(3.14159257373023116985197793156,
                            FieldVector3D.angle(createVector(1, 2, 3, 3), createVector(-2, -4, -6.000001, 3)).getReal(),
                            1.0e-12);
        try {
            FieldVector3D.angle(createVector(0, 0, 0, 3), createVector(1, 0, 0, 3));
            Assert.fail("an exception should have been thrown");
        } catch (MathArithmeticException ae) {
            
        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testAccurateDotProduct
    public void testAccurateDotProduct() {
        
        
        
        FieldVector3D<DerivativeStructure> u1 = createVector(-1321008684645961.0 /  268435456.0,
                                   -5774608829631843.0 /  268435456.0,
                                   -7645843051051357.0 / 8589934592.0, 3);
        FieldVector3D<DerivativeStructure> u2 = createVector(-5712344449280879.0 /    2097152.0,
                                   -4550117129121957.0 /    2097152.0,
                                    8846951984510141.0 /     131072.0, 3);
        DerivativeStructure sNaive = u1.getX().multiply(u2.getX()).add(u1.getY().multiply(u2.getY())).add(u1.getZ().multiply(u2.getZ()));
        DerivativeStructure sAccurate = FieldVector3D.dotProduct(u1, u2);
        Assert.assertEquals(0.0, sNaive.getReal(), 1.0e-30);
        Assert.assertEquals(-2088690039198397.0 / 1125899906842624.0, sAccurate.getReal(), 1.0e-16);
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testDotProduct
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

            FieldVector3D<DerivativeStructure> uds = createVector(ux, uy, uz, 3);
            FieldVector3D<DerivativeStructure> vds = createVector(vx, vy, vz, 3);
            Vector3D v = new Vector3D(vx, vy, vz);

            DerivativeStructure sAccurate = FieldVector3D.dotProduct(uds, vds);
            Assert.assertEquals(sNaive, sAccurate.getReal(), 2.5e-16 * sNaive);
            Assert.assertEquals(ux + vx, sAccurate.getPartialDerivative(1, 0, 0), 2.5e-16 * sNaive);
            Assert.assertEquals(uy + vy, sAccurate.getPartialDerivative(0, 1, 0), 2.5e-16 * sNaive);
            Assert.assertEquals(uz + vz, sAccurate.getPartialDerivative(0, 0, 1), 2.5e-16 * sNaive);

            sAccurate = FieldVector3D.dotProduct(uds, v);
            Assert.assertEquals(sNaive, sAccurate.getReal(), 2.5e-16 * sNaive);
            Assert.assertEquals(vx, sAccurate.getPartialDerivative(1, 0, 0), 2.5e-16 * sNaive);
            Assert.assertEquals(vy, sAccurate.getPartialDerivative(0, 1, 0), 2.5e-16 * sNaive);
            Assert.assertEquals(vz, sAccurate.getPartialDerivative(0, 0, 1), 2.5e-16 * sNaive);

        }
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testAccurateCrossProduct
    public void testAccurateCrossProduct() {
        
        
        
        
        
        final FieldVector3D<DerivativeStructure> u1 = createVector(-1321008684645961.0 /   268435456.0,
                                         -5774608829631843.0 /   268435456.0,
                                         -7645843051051357.0 /  8589934592.0, 3);
        final FieldVector3D<DerivativeStructure> u2 = createVector( 1796571811118507.0 /  2147483648.0,
                                          7853468008299307.0 /  2147483648.0,
                                          2599586637357461.0 / 17179869184.0, 3);
        final FieldVector3D<DerivativeStructure> u3 = createVector(12753243807587107.0 / 18446744073709551616.0, 
                                         -2313766922703915.0 / 18446744073709551616.0, 
                                          -227970081415313.0 /   288230376151711744.0, 3);
        FieldVector3D<DerivativeStructure> cNaive = new FieldVector3D<DerivativeStructure>(u1.getY().multiply(u2.getZ()).subtract(u1.getZ().multiply(u2.getY())),
                                       u1.getZ().multiply(u2.getX()).subtract(u1.getX().multiply(u2.getZ())),
                                       u1.getX().multiply(u2.getY()).subtract(u1.getY().multiply(u2.getX())));
        FieldVector3D<DerivativeStructure> cAccurate = FieldVector3D.crossProduct(u1, u2);
        Assert.assertTrue(FieldVector3D.distance(u3, cNaive).getReal() > 2.9 * u3.getNorm().getReal());
        Assert.assertEquals(0.0, FieldVector3D.distance(u3, cAccurate).getReal(), 1.0e-30 * cAccurate.getNorm().getReal());
    }

// org.apache.commons.math3.geometry.euclidean.threed.FieldVector3DTest::testCrossProduct
    public void testCrossProduct() {
        
        
        Well1024a random = new Well1024a(885362227452043214l);
        for (int i = 0; i < 10000; ++i) {
            double ux = random.nextDouble();
            double uy = random.nextDouble();
            double uz = random.nextDouble();
            double vx = random.nextDouble();
            double vy = random.nextDouble();
            double vz = random.nextDouble();
            Vector3D cNaive = new Vector3D(uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx);

            FieldVector3D<DerivativeStructure> uds = createVector(ux, uy, uz, 3);
            FieldVector3D<DerivativeStructure> vds = createVector(vx, vy, vz, 3);
            Vector3D v = new Vector3D(vx, vy, vz);

            checkVector(FieldVector3D.crossProduct(uds, vds),
                        cNaive.getX(), cNaive.getY(), cNaive.getZ(),
                        0, vz - uz, uy - vy,
                        uz - vz, 0, vx - ux,
                        vy - uy, ux - vx, 0);

            checkVector(FieldVector3D.crossProduct(uds, v),
                        cNaive.getX(), cNaive.getY(), cNaive.getZ(),
                          0,  vz, -vy,
                        -vz,   0,  vx,
                         vy, -vx,   0);

        }
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

// org.apache.commons.math3.geometry.euclidean.threed.LineTest::testRevert
    public void testRevert() {
        
        
        Line line = new Line(new Vector3D(1653345.6696423641, 6170370.041579291, 90000),
                             new Vector3D(1650757.5050732433, 6160710.879908984, 0.9));
        Vector3D expected = line.getDirection().negate();

        
        Line reverted = line.revert();

        
        Assert.assertArrayEquals(expected.toArray(), reverted.getDirection().toArray(), 0);

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

// org.apache.commons.math3.geometry.euclidean.threed.SubLineTest::testIntersectionNotIntersecting
    public void testIntersectionNotIntersecting() throws MathIllegalArgumentException {
        SubLine sub1 = new SubLine(new Vector3D(1, 1, 1), new Vector3D(1.5, 1, 1));
        SubLine sub2 = new SubLine(new Vector3D(2, 3, 0), new Vector3D(2, 3, 0.5));
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

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testSpace
    public void testSpace() {
        Space space = new Vector3D(1, 2, 2).getSpace();
        Assert.assertEquals(3, space.getDimension());
        Assert.assertEquals(2, space.getSubSpace().getDimension());
        Space deserialized = (Space) TestUtils.serializeAndRecover(space);
        Assert.assertTrue(space == deserialized);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testZero
    public void testZero() {
        Assert.assertEquals(0, new Vector3D(1, 2, 2).getZero().getNorm(), 1.0e-15);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testEquals
    public void testEquals() {
        Vector3D u1 = new Vector3D(1, 2, 3);
        Vector3D u2 = new Vector3D(1, 2, 3);
        Assert.assertTrue(u1.equals(u1));
        Assert.assertTrue(u1.equals(u2));
        Assert.assertFalse(u1.equals(new Rotation(1, 0, 0, 0, false)));
        Assert.assertFalse(u1.equals(new Vector3D(1, 2, 3 + 10 * Precision.EPSILON)));
        Assert.assertFalse(u1.equals(new Vector3D(1, 2 + 10 * Precision.EPSILON, 3)));
        Assert.assertFalse(u1.equals(new Vector3D(1 + 10 * Precision.EPSILON, 2, 3)));
        Assert.assertTrue(new Vector3D(0, Double.NaN, 0).equals(new Vector3D(0, 0, Double.NaN)));
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testHash
    public void testHash() {
        Assert.assertEquals(new Vector3D(0, Double.NaN, 0).hashCode(), new Vector3D(0, 0, Double.NaN).hashCode());
        Vector3D u = new Vector3D(1, 2, 3);
        Vector3D v = new Vector3D(1, 2, 3 + 10 * Precision.EPSILON);
        Assert.assertTrue(u.hashCode() != v.hashCode());
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testInfinite
    public void testInfinite() {
        Assert.assertTrue(new Vector3D(1, 1, Double.NEGATIVE_INFINITY).isInfinite());
        Assert.assertTrue(new Vector3D(1, Double.NEGATIVE_INFINITY, 1).isInfinite());
        Assert.assertTrue(new Vector3D(Double.NEGATIVE_INFINITY, 1, 1).isInfinite());
        Assert.assertFalse(new Vector3D(1, 1, 2).isInfinite());
        Assert.assertFalse(new Vector3D(1, Double.NaN, Double.NEGATIVE_INFINITY).isInfinite());
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testNaN
    public void testNaN() {
        Assert.assertTrue(new Vector3D(1, 1, Double.NaN).isNaN());
        Assert.assertTrue(new Vector3D(1, Double.NaN, 1).isNaN());
        Assert.assertTrue(new Vector3D(Double.NaN, 1, 1).isNaN());
        Assert.assertFalse(new Vector3D(1, 1, 2).isNaN());
        Assert.assertFalse(new Vector3D(1, 1, Double.NEGATIVE_INFINITY).isNaN());
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testToString
    public void testToString() {
        Assert.assertEquals("{3; 2; 1}", new Vector3D(3, 2, 1).toString());
        NumberFormat format = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));
        Assert.assertEquals("{3.000; 2.000; 1.000}", new Vector3D(3, 2, 1).toString(format));
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

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testNormSq
    public void testNormSq() {
        Assert.assertEquals(0.0, new Vector3D(0, 0, 0).getNormSq(), 0);
        Assert.assertEquals(14, new Vector3D(1, 2, 3).getNormSq(), 1.0e-12);
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

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testNegate
    public void testNegate() {
        checkVector(new Vector3D(0.1, 2.5, 1.3).negate(), -0.1, -2.5, -1.3);
    }

// org.apache.commons.math3.geometry.euclidean.threed.Vector3DTest::testOrthogonal
    public void testOrthogonal() throws MathArithmeticException {
        Vector3D v1 = new Vector3D(0.1, 2.5, 1.3);
        Assert.assertEquals(0.0, Vector3D.dotProduct(v1, v1.orthogonal()), 1.0e-12);
        Vector3D v2 = new Vector3D(2.3, -0.003, 7.6);
        Assert.assertEquals(0.0, Vector3D.dotProduct(v2, v2.orthogonal()), 1.0e-12);
        Vector3D v3 = new Vector3D(-1.7, 1.4, 0.2);
        Assert.assertEquals(0.0, Vector3D.dotProduct(v3, v3.orthogonal()), 1.0e-12);
        Vector3D v4 = new Vector3D(4.2, 0.1, -1.8);
        Assert.assertEquals(0.0, Vector3D.dotProduct(v4, v4.orthogonal()), 1.0e-12);
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

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testSerial
    public void testSerial()  {
        ArrayFieldVector<Fraction> v = new ArrayFieldVector<Fraction>(vec1);
        Assert.assertEquals(v,TestUtils.serializeAndRecover(v));
    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testZeroVectors
    public void testZeroVectors() {

        
        try {
            new ArrayFieldVector<Fraction>(new Fraction[0]);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            new ArrayFieldVector<Fraction>(new Fraction[0], true);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
        try {
            new ArrayFieldVector<Fraction>(new Fraction[0], false);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        
        Assert.assertEquals(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), new Fraction[0]).getDimension());
        Assert.assertEquals(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), new Fraction[0], true).getDimension());
        Assert.assertEquals(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), new Fraction[0], false).getDimension());

    }

// org.apache.commons.math3.linear.ArrayFieldVectorTest::testOuterProduct
    public void testOuterProduct() {
        final ArrayFieldVector<Fraction> u
            = new ArrayFieldVector<Fraction>(FractionField.getInstance(),
                                             new Fraction[] {new Fraction(1),
                                                             new Fraction(2),
                                                             new Fraction(-3)});
        final ArrayFieldVector<Fraction> v
            = new ArrayFieldVector<Fraction>(FractionField.getInstance(),
                                             new Fraction[] {new Fraction(4),
                                                             new Fraction(-2)});

        final FieldMatrix<Fraction> uv = u.outerProduct(v);

        final double tol = Math.ulp(1d);
        Assert.assertEquals(new Fraction(4).doubleValue(), uv.getEntry(0, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-2).doubleValue(), uv.getEntry(0, 1).doubleValue(), tol);
        Assert.assertEquals(new Fraction(8).doubleValue(), uv.getEntry(1, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-4).doubleValue(), uv.getEntry(1, 1).doubleValue(), tol);
        Assert.assertEquals(new Fraction(-12).doubleValue(), uv.getEntry(2, 0).doubleValue(), tol);
        Assert.assertEquals(new Fraction(6).doubleValue(), uv.getEntry(2, 1).doubleValue(), tol);
    }

// org.apache.commons.math3.linear.ArrayRealVectorTest::testConstructors
    public void testConstructors() {
        final double[] vec1 = {1d, 2d, 3d};
        final double[] vec3 = {7d, 8d, 9d};
        final double[] vec4 = {1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d};
        final Double[] dvec1 = {1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d};

        ArrayRealVector v0 = new ArrayRealVector();
        Assert.assertEquals("testData len", 0, v0.getDimension());

        ArrayRealVector v1 = new ArrayRealVector(7);
        Assert.assertEquals("testData len", 7, v1.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v1.getEntry(6), 0);

        ArrayRealVector v2 = new ArrayRealVector(5, 1.23);
        Assert.assertEquals("testData len", 5, v2.getDimension());
        Assert.assertEquals("testData is 1.23 ", 1.23, v2.getEntry(4), 0);

        ArrayRealVector v3 = new ArrayRealVector(vec1);
        Assert.assertEquals("testData len", 3, v3.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3.getEntry(1), 0);

        ArrayRealVector v3_bis = new ArrayRealVector(vec1, true);
        Assert.assertEquals("testData len", 3, v3_bis.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3_bis.getEntry(1), 0);
        Assert.assertNotSame(v3_bis.getDataRef(), vec1);
        Assert.assertNotSame(v3_bis.toArray(), vec1);

        ArrayRealVector v3_ter = new ArrayRealVector(vec1, false);
        Assert.assertEquals("testData len", 3, v3_ter.getDimension());
        Assert.assertEquals("testData is 2.0 ", 2.0, v3_ter.getEntry(1), 0);
        Assert.assertSame(v3_ter.getDataRef(), vec1);
        Assert.assertNotSame(v3_ter.toArray(), vec1);

        ArrayRealVector v4 = new ArrayRealVector(vec4, 3, 2);
        Assert.assertEquals("testData len", 2, v4.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v4.getEntry(0), 0);
        try {
            new ArrayRealVector(vec4, 8, 3);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        RealVector v5_i = new ArrayRealVector(dvec1);
        Assert.assertEquals("testData len", 9, v5_i.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v5_i.getEntry(8), 0);

        ArrayRealVector v5 = new ArrayRealVector(dvec1);
        Assert.assertEquals("testData len", 9, v5.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v5.getEntry(8), 0);

        ArrayRealVector v6 = new ArrayRealVector(dvec1, 3, 2);
        Assert.assertEquals("testData len", 2, v6.getDimension());
        Assert.assertEquals("testData is 4.0 ", 4.0, v6.getEntry(0), 0);
        try {
            new ArrayRealVector(dvec1, 8, 3);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }

        ArrayRealVector v7 = new ArrayRealVector(v1);
        Assert.assertEquals("testData len", 7, v7.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v7.getEntry(6), 0);

        RealVectorTestImpl v7_i = new RealVectorTestImpl(vec1);

        ArrayRealVector v7_2 = new ArrayRealVector(v7_i);
        Assert.assertEquals("testData len", 3, v7_2.getDimension());
        Assert.assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1), 0);

        ArrayRealVector v8 = new ArrayRealVector(v1, true);
        Assert.assertEquals("testData len", 7, v8.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6), 0);
        Assert.assertNotSame("testData not same object ", v1.getDataRef(), v8.getDataRef());

        ArrayRealVector v8_2 = new ArrayRealVector(v1, false);
        Assert.assertEquals("testData len", 7, v8_2.getDimension());
        Assert.assertEquals("testData is 0.0 ", 0.0, v8_2.getEntry(6), 0);
        Assert.assertEquals("testData same object ", v1.getDataRef(), v8_2.getDataRef());

        ArrayRealVector v9 = new ArrayRealVector(v1, v3);
        Assert.assertEquals("testData len", 10, v9.getDimension());
        Assert.assertEquals("testData is 1.0 ", 1.0, v9.getEntry(7), 0);

        ArrayRealVector v10 = new ArrayRealVector(v2, new RealVectorTestImpl(vec3));
        Assert.assertEquals("testData len", 8, v10.getDimension());
        Assert.assertEquals("testData is 1.23 ", 1.23, v10.getEntry(4), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v10.getEntry(5), 0);

        ArrayRealVector v11 = new ArrayRealVector(new RealVectorTestImpl(vec3), v2);
        Assert.assertEquals("testData len", 8, v11.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v11.getEntry(2), 0);
        Assert.assertEquals("testData is 1.23 ", 1.23, v11.getEntry(3), 0);

        ArrayRealVector v12 = new ArrayRealVector(v2, vec3);
        Assert.assertEquals("testData len", 8, v12.getDimension());
        Assert.assertEquals("testData is 1.23 ", 1.23, v12.getEntry(4), 0);
        Assert.assertEquals("testData is 7.0 ", 7.0, v12.getEntry(5), 0);

        ArrayRealVector v13 = new ArrayRealVector(vec3, v2);
        Assert.assertEquals("testData len", 8, v13.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v13.getEntry(2), 0);
        Assert.assertEquals("testData is 1.23 ", 1.23, v13.getEntry(3), 0);

        ArrayRealVector v14 = new ArrayRealVector(vec3, vec4);
        Assert.assertEquals("testData len", 12, v14.getDimension());
        Assert.assertEquals("testData is 9.0 ", 9.0, v14.getEntry(2), 0);
        Assert.assertEquals("testData is 1.0 ", 1.0, v14.getEntry(3), 0);

    }

// org.apache.commons.math3.linear.ArrayRealVectorTest::testGetDataRef
    public void testGetDataRef() {
        final double[] data = {1d, 2d, 3d, 4d};
        final ArrayRealVector v = new ArrayRealVector(data);
        v.getDataRef()[0] = 0d;
        Assert.assertEquals("", 0d, v.getEntry(0), 0);
    }

// org.apache.commons.math3.linear.ArrayRealVectorTest::testPredicates
    public void testPredicates() {

        Assert.assertEquals(create(new double[] { Double.NaN, 1, 2 }).hashCode(),
                     create(new double[] { 0, Double.NaN, 2 }).hashCode());

        Assert.assertTrue(create(new double[] { Double.NaN, 1, 2 }).hashCode() !=
                   create(new double[] { 0, 1, 2 }).hashCode());
    }

// org.apache.commons.math3.linear.ArrayRealVectorTest::testZeroVectors
    public void testZeroVectors() {
        Assert.assertEquals(0, new ArrayRealVector(new double[0]).getDimension());
        Assert.assertEquals(0, new ArrayRealVector(new double[0], true).getDimension());
        Assert.assertEquals(0, new ArrayRealVector(new double[0], false).getDimension());
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testDimensions
    public void testDimensions() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        Assert.assertEquals("testData row dimension",3,m.getRowDimension());
        Assert.assertEquals("testData column dimension",3,m.getColumnDimension());
        Assert.assertTrue("testData is square",m.isSquare());
        Assert.assertEquals("testData2 row dimension",m2.getRowDimension(),2);
        Assert.assertEquals("testData2 column dimension",m2.getColumnDimension(),3);
        Assert.assertTrue("testData2 is not square",!m2.isSquare());
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testCopyFunctions
    public void testCopyFunctions() {
        Random r = new Random(66636328996002l);
        BlockFieldMatrix<Fraction> m1 = createRandomMatrix(r, 47, 83);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(m1.getData());
        Assert.assertEquals(m1, m2);
        BlockFieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(m3.getData());
        Assert.assertEquals(m3, m4);
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testAdd
    public void testAdd() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        FieldMatrix<Fraction> mPlusMInv = m.add(mInv);
        Fraction[][] sumEntries = mPlusMInv.getData();
        for (int row = 0; row < m.getRowDimension(); row++) {
            for (int col = 0; col < m.getColumnDimension(); col++) {
                Assert.assertEquals(testDataPlusInv[row][col],sumEntries[row][col]);
            }
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testAddFail
    public void testAddFail() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        try {
            m.add(m2);
            Assert.fail("MathIllegalArgumentException expected");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testPlusMinus
    public void testPlusMinus() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testDataInv);
        TestUtils.assertEquals(m.subtract(m2), m2.scalarMultiply(new Fraction(-1)).add(m));
        try {
            m.subtract(new BlockFieldMatrix<Fraction>(testData2));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testMultiply
    public void testMultiply() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        BlockFieldMatrix<Fraction> identity = new BlockFieldMatrix<Fraction>(id);
        BlockFieldMatrix<Fraction> m2 = new BlockFieldMatrix<Fraction>(testData2);
        TestUtils.assertEquals(m.multiply(mInv), identity);
        TestUtils.assertEquals(mInv.multiply(m), identity);
        TestUtils.assertEquals(m.multiply(identity), m);
        TestUtils.assertEquals(identity.multiply(mInv), mInv);
        TestUtils.assertEquals(m2.multiply(identity), m2);
        try {
            m.multiply(new BlockFieldMatrix<Fraction>(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSeveralBlocks
    public void testSeveralBlocks() {
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), 37, 41);
        for (int i = 0; i < m.getRowDimension(); ++i) {
            for (int j = 0; j < m.getColumnDimension(); ++j) {
                m.setEntry(i, j, new Fraction(i * 11 + j, 11));
            }
        }

        FieldMatrix<Fraction> mT = m.transpose();
        Assert.assertEquals(m.getRowDimension(), mT.getColumnDimension());
        Assert.assertEquals(m.getColumnDimension(), mT.getRowDimension());
        for (int i = 0; i < mT.getRowDimension(); ++i) {
            for (int j = 0; j < mT.getColumnDimension(); ++j) {
                Assert.assertEquals(m.getEntry(j, i), mT.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mPm = m.add(m);
        for (int i = 0; i < mPm.getRowDimension(); ++i) {
            for (int j = 0; j < mPm.getColumnDimension(); ++j) {
                Assert.assertEquals(m.getEntry(i, j).multiply(new Fraction(2)), mPm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mPmMm = mPm.subtract(m);
        for (int i = 0; i < mPmMm.getRowDimension(); ++i) {
            for (int j = 0; j < mPmMm.getColumnDimension(); ++j) {
                Assert.assertEquals(m.getEntry(i, j), mPmMm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mTm = mT.multiply(m);
        for (int i = 0; i < mTm.getRowDimension(); ++i) {
            for (int j = 0; j < mTm.getColumnDimension(); ++j) {
                Fraction sum = Fraction.ZERO;
                for (int k = 0; k < mT.getColumnDimension(); ++k) {
                    sum = sum.add(new Fraction(k * 11 + i, 11).multiply(new Fraction(k * 11 + j, 11)));
                }
                Assert.assertEquals(sum, mTm.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> mmT = m.multiply(mT);
        for (int i = 0; i < mmT.getRowDimension(); ++i) {
            for (int j = 0; j < mmT.getColumnDimension(); ++j) {
                Fraction sum = Fraction.ZERO;
                for (int k = 0; k < m.getColumnDimension(); ++k) {
                    sum = sum.add(new Fraction(i * 11 + k, 11).multiply(new Fraction(j * 11 + k, 11)));
                }
                Assert.assertEquals(sum, mmT.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub1 = m.getSubMatrix(2, 9, 5, 20);
        for (int i = 0; i < sub1.getRowDimension(); ++i) {
            for (int j = 0; j < sub1.getColumnDimension(); ++j) {
                Assert.assertEquals(new Fraction((i + 2) * 11 + (j + 5), 11), sub1.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub2 = m.getSubMatrix(10, 12, 3, 40);
        for (int i = 0; i < sub2.getRowDimension(); ++i) {
            for (int j = 0; j < sub2.getColumnDimension(); ++j) {
                Assert.assertEquals(new Fraction((i + 10) * 11 + (j + 3), 11), sub2.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub3 = m.getSubMatrix(30, 34, 0, 5);
        for (int i = 0; i < sub3.getRowDimension(); ++i) {
            for (int j = 0; j < sub3.getColumnDimension(); ++j) {
                Assert.assertEquals(new Fraction((i + 30) * 11 + (j + 0), 11), sub3.getEntry(i, j));
            }
        }

        FieldMatrix<Fraction> sub4 = m.getSubMatrix(30, 32, 32, 35);
        for (int i = 0; i < sub4.getRowDimension(); ++i) {
            for (int j = 0; j < sub4.getColumnDimension(); ++j) {
                Assert.assertEquals(new Fraction((i + 30) * 11 + (j + 32), 11), sub4.getEntry(i, j));
            }
        }

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testMultiply2
    public void testMultiply2() {
       FieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(d3);
       FieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(d4);
       FieldMatrix<Fraction> m5 = new BlockFieldMatrix<Fraction>(d5);
       TestUtils.assertEquals(m3.multiply(m4), m5);
   }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testTrace
    public void testTrace() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(id);
        Assert.assertEquals(new Fraction(3),m.getTrace());
        m = new BlockFieldMatrix<Fraction>(testData2);
        try {
            m.getTrace();
            Assert.fail("Expecting NonSquareMatrixException");
        } catch (NonSquareMatrixException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testScalarAdd
    public void testScalarAdd() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(new BlockFieldMatrix<Fraction>(testDataPlus2),
                               m.scalarAdd(new Fraction(2)));
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testOperate
    public void testOperate() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(testVector, m.operate(testVector));
        TestUtils.assertEquals(testVector, m.operate(new ArrayFieldVector<Fraction>(testVector)).getData());
        m = new BlockFieldMatrix<Fraction>(bigSingular);
        try {
            m.operate(testVector);
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testOperateLarge
    public void testOperateLarge() {
        int p = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int q = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int r =  BlockFieldMatrix.BLOCK_SIZE / 2;
        Random random = new Random(111007463902334l);
        FieldMatrix<Fraction> m1 = createRandomMatrix(random, p, q);
        FieldMatrix<Fraction> m2 = createRandomMatrix(random, q, r);
        FieldMatrix<Fraction> m1m2 = m1.multiply(m2);
        for (int i = 0; i < r; ++i) {
            TestUtils.assertEquals(m1m2.getColumn(i), m1.operate(m2.getColumn(i)));
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testOperatePremultiplyLarge
    public void testOperatePremultiplyLarge() {
        int p = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int q = (11 * BlockFieldMatrix.BLOCK_SIZE) / 10;
        int r =  BlockFieldMatrix.BLOCK_SIZE / 2;
        Random random = new Random(111007463902334l);
        FieldMatrix<Fraction> m1 = createRandomMatrix(random, p, q);
        FieldMatrix<Fraction> m2 = createRandomMatrix(random, q, r);
        FieldMatrix<Fraction> m1m2 = m1.multiply(m2);
        for (int i = 0; i < p; ++i) {
            TestUtils.assertEquals(m1m2.getRow(i), m2.preMultiply(m1.getRow(i)));
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testMath209
    public void testMath209() {
        FieldMatrix<Fraction> a = new BlockFieldMatrix<Fraction>(new Fraction[][] {
                { new Fraction(1), new Fraction(2) },
                { new Fraction(3), new Fraction(4) },
                { new Fraction(5), new Fraction(6) }
        });
        Fraction[] b = a.operate(new Fraction[] { new Fraction(1), new Fraction(1) });
        Assert.assertEquals(a.getRowDimension(), b.length);
        Assert.assertEquals( new Fraction(3), b[0]);
        Assert.assertEquals( new Fraction(7), b[1]);
        Assert.assertEquals(new Fraction(11), b[2]);
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testTranspose
    public void testTranspose() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        FieldMatrix<Fraction> mIT = new FieldLUDecomposition<Fraction>(m).getSolver().getInverse().transpose();
        FieldMatrix<Fraction> mTI = new FieldLUDecomposition<Fraction>(m.transpose()).getSolver().getInverse();
        TestUtils.assertEquals(mIT, mTI);
        m = new BlockFieldMatrix<Fraction>(testData2);
        FieldMatrix<Fraction> mt = new BlockFieldMatrix<Fraction>(testData2T);
        TestUtils.assertEquals(mt, m.transpose());
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testPremultiplyVector
    public void testPremultiplyVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.preMultiply(testVector), preMultTest);
        TestUtils.assertEquals(m.preMultiply(new ArrayFieldVector<Fraction>(testVector).getData()),
                               preMultTest);
        m = new BlockFieldMatrix<Fraction>(bigSingular);
        try {
            m.preMultiply(testVector);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testPremultiply
    public void testPremultiply() {
        FieldMatrix<Fraction> m3 = new BlockFieldMatrix<Fraction>(d3);
        FieldMatrix<Fraction> m4 = new BlockFieldMatrix<Fraction>(d4);
        FieldMatrix<Fraction> m5 = new BlockFieldMatrix<Fraction>(d5);
        TestUtils.assertEquals(m4.preMultiply(m3), m5);

        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> mInv = new BlockFieldMatrix<Fraction>(testDataInv);
        BlockFieldMatrix<Fraction> identity = new BlockFieldMatrix<Fraction>(id);
        TestUtils.assertEquals(m.preMultiply(mInv), identity);
        TestUtils.assertEquals(mInv.preMultiply(m), identity);
        TestUtils.assertEquals(m.preMultiply(identity), m);
        TestUtils.assertEquals(identity.preMultiply(mInv), mInv);
        try {
            m.preMultiply(new BlockFieldMatrix<Fraction>(bigSingular));
            Assert.fail("Expecting illegalArgumentException");
        } catch (MathIllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetVectors
    public void testGetVectors() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        TestUtils.assertEquals(m.getRow(0), testDataRow1);
        TestUtils.assertEquals(m.getColumn(2), testDataCol3);
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

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetEntry
    public void testGetEntry() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        Assert.assertEquals(m.getEntry(0,1),new Fraction(2));
        try {
            m.getEntry(10, 4);
            Assert.fail ("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testExamples
    public void testExamples() {
        
        Fraction[][] matrixData = {
                {new Fraction(1),new Fraction(2),new Fraction(3)},
                {new Fraction(2),new Fraction(5),new Fraction(3)}
        };
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(matrixData);
        
        Fraction[][] matrixData2 = {
                {new Fraction(1),new Fraction(2)},
                {new Fraction(2),new Fraction(5)},
                {new Fraction(1), new Fraction(7)}
        };
        FieldMatrix<Fraction> n = new BlockFieldMatrix<Fraction>(matrixData2);
        
        FieldMatrix<Fraction> p = m.multiply(n);
        Assert.assertEquals(2, p.getRowDimension());
        Assert.assertEquals(2, p.getColumnDimension());
        
        FieldMatrix<Fraction> pInverse = new FieldLUDecomposition<Fraction>(p).getSolver().getInverse();
        Assert.assertEquals(2, pInverse.getRowDimension());
        Assert.assertEquals(2, pInverse.getColumnDimension());

        
        Fraction[][] coefficientsData = {
                {new Fraction(2), new Fraction(3), new Fraction(-2)},
                {new Fraction(-1), new Fraction(7), new Fraction(6)},
                {new Fraction(4), new Fraction(-3), new Fraction(-5)}
        };
        FieldMatrix<Fraction> coefficients = new BlockFieldMatrix<Fraction>(coefficientsData);
        Fraction[] constants = {
            new Fraction(1), new Fraction(-2), new Fraction(1)
        };
        Fraction[] solution;
        solution = new FieldLUDecomposition<Fraction>(coefficients)
            .getSolver()
            .solve(new ArrayFieldVector<Fraction>(constants, false)).toArray();
        Assert.assertEquals(new Fraction(2).multiply(solution[0]).
                     add(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(2).multiply(solution[2])),
                     constants[0]);
        Assert.assertEquals(new Fraction(-1).multiply(solution[0]).
                     add(new Fraction(7).multiply(solution[1])).
                     add(new Fraction(6).multiply(solution[2])),
                     constants[1]);
        Assert.assertEquals(new Fraction(4).multiply(solution[0]).
                     subtract(new Fraction(3).multiply(solution[1])).
                     subtract(new Fraction(5).multiply(solution[2])),
                     constants[2]);

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSubMatrix
    public void testGetSubMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
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

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetMatrixLarge
    public void testGetSetMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n - 4, n - 4).scalarAdd(new Fraction(1));

        m.setSubMatrix(sub.getData(), 2, 2);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((i < 2) || (i > n - 3) || (j < 2) || (j > n - 3)) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        Assert.assertEquals(sub, m.getSubMatrix(2, n - 3, 2, n - 3));
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testCopySubMatrix
    public void testCopySubMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
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
        checkCopy(m, null, new int[] {}, new int[] { 0 });
        checkCopy(m, null, new int[] { 0 }, new int[] { 4 });
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetRowMatrix
    public void testGetRowMatrix() {
        FieldMatrix<Fraction> m     = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow0 = new BlockFieldMatrix<Fraction>(subRow0);
        FieldMatrix<Fraction> mRow3 = new BlockFieldMatrix<Fraction>(subRow3);
        Assert.assertEquals("Row0", mRow0, m.getRowMatrix(0));
        Assert.assertEquals("Row3", mRow3, m.getRowMatrix(3));
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

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetRowMatrix
    public void testSetRowMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mRow3 = new BlockFieldMatrix<Fraction>(subRow3);
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

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetRowMatrixLarge
    public void testGetSetRowMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), 1, n).scalarAdd(new Fraction(1));

        m.setRowMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        Assert.assertEquals(sub, m.getRowMatrix(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetColumnMatrix
    public void testGetColumnMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn1 = new BlockFieldMatrix<Fraction>(subColumn1);
        FieldMatrix<Fraction> mColumn3 = new BlockFieldMatrix<Fraction>(subColumn3);
        Assert.assertEquals(mColumn1, m.getColumnMatrix(1));
        Assert.assertEquals(mColumn3, m.getColumnMatrix(3));
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

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetColumnMatrix
    public void testSetColumnMatrix() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldMatrix<Fraction> mColumn3 = new BlockFieldMatrix<Fraction>(subColumn3);
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

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetColumnMatrixLarge
    public void testGetSetColumnMatrixLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldMatrix<Fraction> sub =
            new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, 1).scalarAdd(new Fraction(1));

        m.setColumnMatrix(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        Assert.assertEquals(sub, m.getColumnMatrix(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetRowVector
    public void testGetRowVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow0 = new ArrayFieldVector<Fraction>(subRow0[0]);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        Assert.assertEquals(mRow0, m.getRowVector(0));
        Assert.assertEquals(mRow3, m.getRowVector(3));
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

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetRowVector
    public void testSetRowVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mRow3 = new ArrayFieldVector<Fraction>(subRow3[0]);
        Assert.assertNotSame(mRow3, m.getRowMatrix(0));
        m.setRowVector(0, mRow3);
        Assert.assertEquals(mRow3, m.getRowVector(0));
        try {
            m.setRowVector(-1, mRow3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRowVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetRowVectorLarge
    public void testGetSetRowVectorLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldVector<Fraction> sub = new ArrayFieldVector<Fraction>(n, new Fraction(1));

        m.setRowVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        Assert.assertEquals(sub, m.getRowVector(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetColumnVector
    public void testGetColumnVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn1 = columnToVector(subColumn1);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        Assert.assertEquals(mColumn1, m.getColumnVector(1));
        Assert.assertEquals(mColumn3, m.getColumnVector(3));
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

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetColumnVector
    public void testSetColumnVector() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        FieldVector<Fraction> mColumn3 = columnToVector(subColumn3);
        Assert.assertNotSame(mColumn3, m.getColumnVector(1));
        m.setColumnVector(1, mColumn3);
        Assert.assertEquals(mColumn3, m.getColumnVector(1));
        try {
            m.setColumnVector(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumnVector(0, new ArrayFieldVector<Fraction>(FractionField.getInstance(), 5));
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetColumnVectorLarge
    public void testGetSetColumnVectorLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        FieldVector<Fraction> sub = new ArrayFieldVector<Fraction>(n, new Fraction(1));

        m.setColumnVector(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        Assert.assertEquals(sub, m.getColumnVector(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetRow
    public void testGetRow() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
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

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetRow
    public void testSetRow() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Assert.assertTrue(subRow3[0][0] != m.getRow(0)[0]);
        m.setRow(0, subRow3[0]);
        checkArrays(subRow3[0], m.getRow(0));
        try {
            m.setRow(-1, subRow3[0]);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setRow(0, new Fraction[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetRowLarge
    public void testGetSetRowLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        Fraction[] sub = new Fraction[n];
        Arrays.fill(sub, new Fraction(1));

        m.setRow(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (i != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        checkArrays(sub, m.getRow(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetColumn
    public void testGetColumn() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn1 = columnToArray(subColumn1);
        Fraction[] mColumn3 = columnToArray(subColumn3);
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

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetColumn
    public void testSetColumn() {
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(subTestData);
        Fraction[] mColumn3 = columnToArray(subColumn3);
        Assert.assertTrue(mColumn3[0] != m.getColumn(1)[0]);
        m.setColumn(1, mColumn3);
        checkArrays(mColumn3, m.getColumn(1));
        try {
            m.setColumn(-1, mColumn3);
            Assert.fail("Expecting OutOfRangeException");
        } catch (OutOfRangeException ex) {
            
        }
        try {
            m.setColumn(0, new Fraction[5]);
            Assert.fail("Expecting MatrixDimensionMismatchException");
        } catch (MatrixDimensionMismatchException ex) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testGetSetColumnLarge
    public void testGetSetColumnLarge() {
        int n = 3 * BlockFieldMatrix.BLOCK_SIZE;
        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), n, n);
        Fraction[] sub = new Fraction[n];
        Arrays.fill(sub, new Fraction(1));

        m.setColumn(2, sub);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j != 2) {
                    Assert.assertEquals(new Fraction(0), m.getEntry(i, j));
                } else {
                    Assert.assertEquals(new Fraction(1), m.getEntry(i, j));
                }
            }
        }
        checkArrays(sub, m.getColumn(2));

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testEqualsAndHashCode
    public void testEqualsAndHashCode() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        BlockFieldMatrix<Fraction> m1 = (BlockFieldMatrix<Fraction>) m.copy();
        BlockFieldMatrix<Fraction> mt = (BlockFieldMatrix<Fraction>) m.transpose();
        Assert.assertTrue(m.hashCode() != mt.hashCode());
        Assert.assertEquals(m.hashCode(), m1.hashCode());
        Assert.assertEquals(m, m);
        Assert.assertEquals(m, m1);
        Assert.assertFalse(m.equals(null));
        Assert.assertFalse(m.equals(mt));
        Assert.assertFalse(m.equals(new BlockFieldMatrix<Fraction>(bigSingular)));
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testToString
    public void testToString() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        Assert.assertEquals("BlockFieldMatrix{{1,2,3},{2,5,3},{1,0,8}}", m.toString());
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSetSubMatrix
    public void testSetSubMatrix() {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        m.setSubMatrix(detData2,1,1);
        FieldMatrix<Fraction> expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(1),new Fraction(2),new Fraction(3)},{new Fraction(2),new Fraction(1),new Fraction(3)},{new Fraction(1),new Fraction(2),new Fraction(4)}});
        Assert.assertEquals(expected, m);

        m.setSubMatrix(detData2,0,0);
        expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(1),new Fraction(3),new Fraction(3)},{new Fraction(2),new Fraction(4),new Fraction(3)},{new Fraction(1),new Fraction(2),new Fraction(4)}});
        Assert.assertEquals(expected, m);

        m.setSubMatrix(testDataPlus2,0,0);
        expected = new BlockFieldMatrix<Fraction>
            (new Fraction[][] {{new Fraction(3),new Fraction(4),new Fraction(5)},{new Fraction(4),new Fraction(7),new Fraction(5)},{new Fraction(3),new Fraction(2),new Fraction(10)}});
        Assert.assertEquals(expected, m);

        
        BlockFieldMatrix<Fraction> matrix =
            new BlockFieldMatrix<Fraction>(new Fraction[][] {
                    {new Fraction(1), new Fraction(2), new Fraction(3), new Fraction(4)},
                    {new Fraction(5), new Fraction(6), new Fraction(7), new Fraction(8)},
                    {new Fraction(9), new Fraction(0), new Fraction(1) , new Fraction(2)}
            });
        matrix.setSubMatrix(new Fraction[][] {
                {new Fraction(3), new Fraction(4)},
                {new Fraction(5), new Fraction(6)}
        }, 1, 1);
        expected =
            new BlockFieldMatrix<Fraction>(new Fraction[][] {
                    {new Fraction(1), new Fraction(2), new Fraction(3),new Fraction(4)},
                    {new Fraction(5), new Fraction(3), new Fraction(4), new Fraction(8)},
                    {new Fraction(9), new Fraction(5) ,new Fraction(6), new Fraction(2)}
            });
        Assert.assertEquals(expected, matrix);

        
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

        
        try {
            m.setSubMatrix(new Fraction[][] {{new Fraction(1)}, {new Fraction(2), new Fraction(3)}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }

        
        try {
            m.setSubMatrix(new Fraction[][] {{}}, 0, 0);
            Assert.fail("expecting MathIllegalArgumentException");
        } catch (MathIllegalArgumentException e) {
            
        }
    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testWalk
    public void testWalk() {
        int rows    = 150;
        int columns = 75;

        FieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor());
        GetVisitor getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInRowOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInColumnOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInOptimizedOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInRowOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor());
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor);
        Assert.assertEquals(rows * columns, getVisitor.getCount());

        m = new BlockFieldMatrix<Fraction>(FractionField.getInstance(), rows, columns);
        m.walkInOptimizedOrder(new SetVisitor(), 1, rows - 2, 1, columns - 2);
        getVisitor = new GetVisitor();
        m.walkInColumnOrder(getVisitor, 1, rows - 2, 1, columns - 2);
        Assert.assertEquals((rows - 2) * (columns - 2), getVisitor.getCount());
        for (int i = 0; i < rows; ++i) {
            Assert.assertEquals(new Fraction(0), m.getEntry(i, 0));
            Assert.assertEquals(new Fraction(0), m.getEntry(i, columns - 1));
        }
        for (int j = 0; j < columns; ++j) {
            Assert.assertEquals(new Fraction(0), m.getEntry(0, j));
            Assert.assertEquals(new Fraction(0), m.getEntry(rows - 1, j));
        }

    }

// org.apache.commons.math3.linear.BlockFieldMatrixTest::testSerial
    public void testSerial()  {
        BlockFieldMatrix<Fraction> m = new BlockFieldMatrix<Fraction>(testData);
        Assert.assertEquals(m,TestUtils.serializeAndRecover(m));
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testNonSquareOperator
    public void testNonSquareOperator() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 3);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        final ArrayRealVector x = new ArrayRealVector(a.getColumnDimension());
        solver.solve(a, b, x);
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testDimensionMismatchRightHandSide
    public void testDimensionMismatchRightHandSide() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(3, 3);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(2);
        final ArrayRealVector x = new ArrayRealVector(3);
        solver.solve(a, b, x);
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testDimensionMismatchSolution
    public void testDimensionMismatchSolution() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(3, 3);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0., false);
        final ArrayRealVector b = new ArrayRealVector(3);
        final ArrayRealVector x = new ArrayRealVector(2);
        solver.solve(a, b, x);
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testNonPositiveDefiniteLinearOperator
    public void testNonPositiveDefiniteLinearOperator() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        a.setEntry(0, 0, -1.);
        a.setEntry(0, 1, 2.);
        a.setEntry(1, 0, 3.);
        a.setEntry(1, 1, 4.);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0., true);
        final ArrayRealVector b = new ArrayRealVector(2);
        b.setEntry(0, -1.);
        b.setEntry(1, -1.);
        final ArrayRealVector x = new ArrayRealVector(2);
        solver.solve(a, b, x);
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testUnpreconditionedSolution
    public void testUnpreconditionedSolution() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(maxIterations, 1E-10, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x = solver.solve(a, b);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-10 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d]", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testUnpreconditionedInPlaceSolutionWithInitialGuess
    public void testUnpreconditionedInPlaceSolutionWithInitialGuess() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(maxIterations, 1E-10, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x0 = new ArrayRealVector(n);
            x0.set(1.);
            final RealVector x = solver.solveInPlace(a, b, x0);
            Assert.assertSame("x should be a reference to x0", x0, x);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-10 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d)", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
            }
        }
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testUnpreconditionedSolutionWithInitialGuess
    public void testUnpreconditionedSolutionWithInitialGuess() {
        final int n = 5;
        final int maxIterations = 100;
        final RealLinearOperator a = new HilbertMatrix(n);
        final InverseHilbertMatrix ainv = new InverseHilbertMatrix(n);
        final IterativeLinearSolver solver;
        solver = new ConjugateGradient(maxIterations, 1E-10, true);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);
            final RealVector x0 = new ArrayRealVector(n);
            x0.set(1.);
            final RealVector x = solver.solve(a, b, x0);
            Assert.assertNotSame("x should not be a reference to x0", x0, x);
            for (int i = 0; i < n; i++) {
                final double actual = x.getEntry(i);
                final double expected = ainv.getEntry(i, j);
                final double delta = 1E-10 * Math.abs(expected);
                final String msg = String.format("entry[%d][%d]", i, j);
                Assert.assertEquals(msg, expected, actual, delta);
                Assert.assertEquals(msg, x0.getEntry(i), 1., Math.ulp(1.));
            }
        }
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testUnpreconditionedResidual
    public void testUnpreconditionedResidual() {
        final int n = 10;
        final int maxIterations = n;
        final RealLinearOperator a = new HilbertMatrix(n);
        final ConjugateGradient solver;
        solver = new ConjugateGradient(maxIterations, 1E-15, true);
        final RealVector r = new ArrayRealVector(n);
        final RealVector x = new ArrayRealVector(n);
        final IterationListener listener = new IterationListener() {

            public void terminationPerformed(final IterationEvent e) {
                
            }

            public void iterationStarted(final IterationEvent e) {
                
            }

            public void iterationPerformed(final IterationEvent e) {
                final IterativeLinearSolverEvent evt;
                evt = (IterativeLinearSolverEvent) e;
                RealVector v = evt.getResidual();
                r.setSubVector(0, v);
                v = evt.getSolution();
                x.setSubVector(0, v);
            }

            public void initializationPerformed(final IterationEvent e) {
                
            }
        };
        solver.getIterationManager().addIterationListener(listener);
        final RealVector b = new ArrayRealVector(n);
        for (int j = 0; j < n; j++) {
            b.set(0.);
            b.setEntry(j, 1.);

            boolean caught = false;
            try {
                solver.solve(a, b);
            } catch (MaxCountExceededException e) {
                caught = true;
                final RealVector y = a.operate(x);
                for (int i = 0; i < n; i++) {
                    final double actual = b.getEntry(i) - y.getEntry(i);
                    final double expected = r.getEntry(i);
                    final double delta = 1E-6 * Math.abs(expected);
                    final String msg = String
                        .format("column %d, residual %d", i, j);
                    Assert.assertEquals(msg, expected, actual, delta);
                }
            }
            Assert
                .assertTrue("MaxCountExceededException should have been caught",
                            caught);
        }
    }

// org.apache.commons.math3.linear.ConjugateGradientTest::testNonSquarePreconditioner
    public void testNonSquarePreconditioner() {
        final Array2DRowRealMatrix a = new Array2DRowRealMatrix(2, 2);
        final RealLinearOperator m = new RealLinearOperator() {

            @Override
            public RealVector operate(final RealVector x) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getRowDimension() {
                return 2;
            }

            @Override
            public int getColumnDimension() {
                return 3;
            }
        };
        final PreconditionedIterativeLinearSolver solver;
        solver = new ConjugateGradient(10, 0d, false);
        final ArrayRealVector b = new ArrayRealVector(a.getRowDimension());
        solver.solve(a, m, b);
    }
