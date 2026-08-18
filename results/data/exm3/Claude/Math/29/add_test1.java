// org/apache/commons/math3/linear/RealVectorAbstractTest.java
@Test
public void testEbeDivideZeroByZero() {
    RealVector v1 = create(new double[] {0.0, 1.0, 2.0});
    RealVector v2 = create(new double[] {0.0, 2.0, 4.0});
    RealVector result = v1.ebeDivide(v2);
    Assert.assertTrue(Double.isNaN(result.getEntry(0)));
    Assert.assertEquals(0.5, result.getEntry(1), 0.0);
    Assert.assertEquals(0.5, result.getEntry(2), 0.0);
}