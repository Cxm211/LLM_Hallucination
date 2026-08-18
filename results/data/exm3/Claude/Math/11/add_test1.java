// org/apache/commons/math3/distribution/MultivariateNormalDistributionTest.java
@Test
public void testTrivariateDistribution() {
    final double[] mu = { 1, 2, 3 };
    final double[][] sigma = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
    
    final MultivariateNormalDistribution multi = new MultivariateNormalDistribution(mu, sigma);
    
    final double expected = 1.0 / Math.pow(2.0 * Math.PI, 1.5);
    final double actual = multi.density(new double[] { 1, 2, 3 });
    final double tol = 1e-9;
    Assert.assertEquals(expected, actual, tol);
}