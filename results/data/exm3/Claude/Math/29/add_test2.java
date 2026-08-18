// org/apache/commons/math3/linear/RealVectorAbstractTest.java
@Test
public void testEbeMultiplyZeroByInfinity() {
    RealVector v1 = create(new double[] {0.0, 1.0, 0.0});
    RealVector v2 = create(new double[] {Double.POSITIVE_INFINITY, 2.0, Double.NEGATIVE_INFINITY});
    RealVector result = v1.ebeMultiply(v2);
    Assert.assertTrue(Double.isNaN(result.getEntry(0)));
    Assert.assertEquals(2.0, result.getEntry(1), 0.0);
    Assert.assertTrue(Double.isNaN(result.getEntry(2)));
}