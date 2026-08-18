// org/apache/commons/math3/analysis/differentiation/DerivativeStructureTest.java
@Test
    public void testAtan2NegativeXPositiveZeroY() {
        DerivativeStructure y = new DerivativeStructure(2, 2, 1, +0.0);
        DerivativeStructure x = new DerivativeStructure(2, 2, 1, -1.0);
        DerivativeStructure result = DerivativeStructure.atan2(y, x);
        Assert.assertEquals(FastMath.PI, result.getValue(), 1.0e-15);
    }
