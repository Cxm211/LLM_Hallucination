// org/apache/commons/math3/distribution/FDistributionTest.java
@Test
    public void testMath785HighProbability() {
        // Test with high probability and large degrees of freedom
        try {
            double prob = 0.99;
            FDistribution f = new FDistribution(500000, 500000);
            double result = f.inverseCumulativeProbability(prob);
            Assert.assertTrue(result > 1.0);
        } catch (Exception e) {
            Assert.fail("Failing to calculate inverse cumulative probability for high probability");
        }
    }