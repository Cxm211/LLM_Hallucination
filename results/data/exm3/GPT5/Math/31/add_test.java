// org/apache/commons/math3/distribution/FDistributionTest.java
@Test
    public void testCdfAtOneLargeDof() {
        FDistribution f = new FDistribution(200000, 200000);
        double p = f.cumulativeProbability(1.0);
        Assert.assertEquals(0.5, p, 1e-8);
    }