// org/apache/commons/math3/analysis/differentiation/DerivativeStructureTest.java::testAtan2InfinityCases
@Test
public void testAtan2InfinityCases() {
    DerivativeStructure y = new DerivativeStructure(2, 2, 1, 1.0);
    DerivativeStructure x = new DerivativeStructure(2, 2, 1, Double.NEGATIVE_INFINITY);
    DerivativeStructure a = DerivativeStructure.atan2(y, x);
    Assert.assertEquals(FastMath.PI, a.getValue(), 1.0e-15);
}