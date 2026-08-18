// org/apache/commons/math/linear/SparseRealVectorTest.java
@Test
public void testEbeDivideWithZeroDivisor() {
    final RealVector u = new OpenMapRealVector(3, 1e-6);
    u.setEntry(0, 0);
    u.setEntry(1, 6);
    u.setEntry(2, 0);

    final double[] v = new double[3];
    v[0] = 2;
    v[1] = 3;
    v[2] = 4;

    RealVector w = u.ebeDivide(v);
    Assert.assertEquals(0.0, w.getEntry(0), 1e-6);
    Assert.assertEquals(2.0, w.getEntry(1), 1e-6);
    Assert.assertEquals(0.0, w.getEntry(2), 1e-6);
}