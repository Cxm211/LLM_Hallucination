// org/apache/commons/math3/distribution/MultivariateNormalDistributionTest.java
@Test
public void testThreeDimensionalStandardAtMean() {
    final double[] mu = { 0, 0, 0 };
    final double[][] sigma = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
    final MultivariateNormalDistribution multi = new MultivariateNormalDistribution(mu, sigma);
    final double[] x = { 0, 0, 0 };
    final double expected = Math.pow(2 * Math.PI, -1.5);
    final double tol = Math.ulp(1d);
    Assert.assertEquals(expected, multi.density(x), tol);
}