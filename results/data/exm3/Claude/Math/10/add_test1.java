// org/apache/commons/math3/analysis/differentiation/DerivativeStructureTest.java
@Test
    public void testAtan2NegativeXBranch() {
        // Test the negative x branch with non-zero y
        DerivativeStructure positiveYNegativeX =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, 1.0),
                                          new DerivativeStructure(2, 2, 1, -1.0));
        Assert.assertEquals(3 * FastMath.PI / 4, positiveYNegativeX.getValue(), 1.0e-15);

        DerivativeStructure negativeYNegativeX =
                DerivativeStructure.atan2(new DerivativeStructure(2, 2, 1, -1.0),
                                          new DerivativeStructure(2, 2, 1, -1.0));
        Assert.assertEquals(-3 * FastMath.PI / 4, negativeYNegativeX.getValue(), 1.0e-15);
    }