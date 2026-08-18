// org/apache/commons/math/linear/RealMatrixImplTest.java
public void testOperateSingleRowMatrix() {
    RealMatrix a = new RealMatrixImpl(new double[][] {
            { 5, 10 }
    }, false);
    double[] b = a.operate(new double[] { 2, 3 });
    assertEquals(1, b.length);
    assertEquals(40.0, b[0], 1.0e-12);
}