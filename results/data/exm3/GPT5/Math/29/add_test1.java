// org/apache/commons/math3/linear/RealVectorAbstractTest.java
@Test
public void testEbeMultiplyZeroWithSpecialValues() {
    OpenMapRealVector a = new OpenMapRealVector(3); // all zeros
    OpenMapRealVector b = new OpenMapRealVector(3);
    b.setEntry(0, Double.NaN);
    b.setEntry(1, Double.POSITIVE_INFINITY);
    b.setEntry(2, 1.0);
    RealVector r = a.ebeMultiply(b);
    Assert.assertTrue(Double.isNaN(r.getEntry(0)));
    Assert.assertTrue(Double.isNaN(r.getEntry(1)));
    Assert.assertEquals(0.0, r.getEntry(2), 0.0);
}