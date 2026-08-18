// org/apache/commons/math3/linear/RealVectorAbstractTest.java
@Test
public void testEbeMultiplyZeroByNaN() {
    RealVector v1 = create(new double[] {0.0, 1.0, 2.0});
    RealVector v2 = create(new double[] {Double.NaN, 3.0, 4.0});
    RealVector result = v1.ebeMultiply(v2);
    Assert.assertTrue(Double.isNaN(result.getEntry(0)));
    Assert.assertEquals(3.0, result.getEntry(1), 0.0);
    Assert.assertEquals(8.0, result.getEntry(2), 0.0);
}