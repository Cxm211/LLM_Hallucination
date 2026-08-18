// org/apache/commons/math3/analysis/differentiation/DerivativeStructureTest.java
@Test
    public void testAtan2NonZeroWithZero() {
        // Test non-zero y with zero x
        DerivativeStructure positiveYZeroX =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, 1.0),
                                          new DerivativeStructure(2, 2, 1, 0.0));
        Assert.assertEquals(FastMath.PI / 2, positiveYZeroX.getValue(), 1.0e-15);

        DerivativeStructure negativeYZeroX =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, -1.0),
                                          new DerivativeStructure(2, 2, 1, 0.0));
        Assert.assertEquals(-FastMath.PI / 2, negativeYZeroX.getValue(), 1.0e-15);

        // Test zero y with non-zero x
        DerivativeStructure zeroYPositiveX =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, 0.0),
                                          new DerivativeStructure(2, 2, 1, 1.0));
        Assert.assertEquals(0.0, zeroYPositiveX.getValue(), 1.0e-15);

        DerivativeStructure zeroYNegativeX =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, 0.0),
                                          new DerivativeStructure(2, 2, 1, -1.0));
        Assert.assertEquals(FastMath.PI, zeroYNegativeX.getValue(), 1.0e-15);
    }