// org/apache/commons/math3/distribution/MultivariateNormalDistributionTest.java
@Test
    public void testMultivariateOddDimension() {
        final int dim = 3;
        final Random rng = new Random(12345);
        final double[] mu = new double[dim];
        final double[][] sigma = new double[dim][dim];
        final double[] variances = new double[dim];
        for (int i = 0; i < dim; i++) {
            mu[i] = rng.nextDouble() * 10 - 5;
            variances[i] = rng.nextDouble() * 5 + 0.1;
            sigma[i][i] = variances[i];
        }
        final MultivariateNormalDistribution multi = new MultivariateNormalDistribution(mu, sigma);
        final NormalDistribution[] univars = new NormalDistribution[dim];
        for (int i = 0; i < dim; i++) {
            univars[i] = new NormalDistribution(mu[i], Math.sqrt(variances[i]));
        }
        final int numCases = 10;
        final double tol = 1e-12;
        for (int k = 0; k < numCases; k++) {
            final double[] vals = new double[dim];
            for (int i = 0; i < dim; i++) {
                vals[i] = rng.nextDouble() * 10 - 5;
            }
            double expected = 1.0;
            for (int i = 0; i < dim; i++) {
                expected *= univars[i].density(vals[i]);
            }
            final double actual = multi.density(vals);
            Assert.assertEquals(expected, actual, tol);
        }
    }
