// org/apache/commons/math3/distribution/BinomialDistributionTest.java
@Test
    public void testLargeTrialsConsistency() {
        int trials = 1000000;
        double p = 0.3;
        BinomialDistribution dist = new BinomialDistribution(trials, p);
        double prob = 0.7;
        int x = dist.inverseCumulativeProbability(prob);
        double actualProb = dist.cumulativeProbability(x);
        Assert.assertEquals(prob, actualProb, 1e-6);
    }
