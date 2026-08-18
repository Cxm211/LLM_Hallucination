// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testBracketWithZeroAtLowerBound() throws MathException {
        NormalDistribution normal = new NormalDistributionImpl(0,1);
        double result = normal.inverseCumulativeProbability(0.5);
        assertEquals(0.0, result, 1.0e-12);
    }