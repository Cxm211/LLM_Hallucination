// org/apache/commons/math/linear/RealMatrixImplTest.java
public void testOperateNonSquareMatrix() {
    RealMatrix a = new RealMatrixImpl(new double[][] {
            { 2, 3, 4 },
            { 1, 0, 2 }
    }, false);
    double[] b = a.operate(new double[] { 1, 2, 3 });
    assertEquals(a.getRowDimension(), b.length);
    assertEquals(20.0, b[0], 1.0e-12);
    assertEquals(7.0, b[1], 1.0e-12);
}