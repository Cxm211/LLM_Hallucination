// org/apache/commons/math3/distribution/MultivariateNormalDistributionTest.java
@Test
public void testBivariateDistribution() {
    final double[] mu = { 0, 0 };
    final double[][] sigma = { { 1, 0 }, { 0, 1 } };
    
    final MultivariateNormalDistribution multi = new MultivariateNormalDistribution(mu, sigma);
    
    final double expected = 1.0 / (2.0 * Math.PI);
    final double actual = multi.density(new double[] { 0, 0 });
    final double tol = 1e-9;
    Assert.assertEquals(expected, actual, tol);
}