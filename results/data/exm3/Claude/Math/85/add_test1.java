// org/apache/commons/math/distribution/NormalDistributionTest.java
public void testBracketWithZeroAtUpperBound() throws MathException {
        NormalDistribution normal = new NormalDistributionImpl(0,1);
        double result = normal.inverseCumulativeProbability(0.5000000000000001);
        assertEquals(0.0, result, 1.0e-9);
    }