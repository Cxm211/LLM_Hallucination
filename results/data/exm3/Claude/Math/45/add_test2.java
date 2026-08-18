// org/apache/commons/math/linear/OpenMapRealMatrixTest.java
@Test
public void testMath679_ValidLargeMatrix() {
    OpenMapRealMatrix m = new OpenMapRealMatrix(46340, 46340);
    assertEquals(46340, m.getRowDimension());
    assertEquals(46340, m.getColumnDimension());
}