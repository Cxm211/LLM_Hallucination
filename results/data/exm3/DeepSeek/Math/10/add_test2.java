// org/apache/commons/math3/analysis/differentiation/DerivativeStructureTest.java
@Test
    public void testAtan2BothPositiveInfinity() {
        DerivativeStructure y = new DerivativeStructure(2, 2, 1, Double.POSITIVE_INFINITY);
        DerivativeStructure x = new DerivativeStructure(2, 2, 1, Double.POSITIVE_INFINITY);
        DerivativeStructure result = DerivativeStructure.atan2(y, x);
        Assert.assertEquals(FastMath.PI/4, result.getValue(), 1.0e-15);
    }
