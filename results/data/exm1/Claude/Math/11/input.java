// buggy code
    public double density(final double[] vals) throws DimensionMismatchException {
        final int dim = getDimension();
        if (vals.length != dim) {
            throw new DimensionMismatchException(vals.length, dim);
        }

        return FastMath.pow(2 * FastMath.PI, -dim / 2) *
            FastMath.pow(covarianceMatrixDeterminant, -0.5) *
            getExponentTerm(vals);
    }

// relevant test
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
