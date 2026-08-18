// org/apache/commons/math3/distribution/FDistributionTest.java
@Test
    public void testLargeDFConsistency() {
        int df1 = 200000;
        int df2 = 100000;
        FDistribution f = new FDistribution(df1, df2);
        double prob = 0.01;
        double x = f.inverseCumulativeProbability(prob);
        double actualProb = f.cumulativeProbability(x);
        Assert.assertEquals(prob, actualProb, 1e-10);
    }
