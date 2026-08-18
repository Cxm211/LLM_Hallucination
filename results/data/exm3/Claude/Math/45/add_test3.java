// org/apache/commons/math/linear/OpenMapRealMatrixTest.java
@Test(expected = NumberIsTooLargeException.class)
public void testMath679_BoundaryExceed() {
    new OpenMapRealMatrix(46341, 46341);
}