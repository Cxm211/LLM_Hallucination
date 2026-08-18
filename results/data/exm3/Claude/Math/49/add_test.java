// org/apache/commons/math/linear/SparseRealVectorTest.java
@Test
public void testEbeMultiplyResultAllZeros() {
    final RealVector u = new OpenMapRealVector(3, 1e-6);
    u.setEntry(0, 5);
    u.setEntry(1, 0);
    u.setEntry(2, 3);

    final RealVector v = new OpenMapRealVector(3, 1e-6);
    v.setEntry(0, 0);
    v.setEntry(1, 2);
    v.setEntry(2, 0);

    RealVector w = u.ebeMultiply(v);
    Assert.assertEquals(0.0, w.getEntry(0), 1e-6);
    Assert.assertEquals(0.0, w.getEntry(1), 1e-6);
    Assert.assertEquals(0.0, w.getEntry(2), 1e-6);
}