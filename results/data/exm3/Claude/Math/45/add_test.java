// org/apache/commons/math/linear/OpenMapRealMatrixTest.java
@Test(expected = NumberIsTooLargeException.class)
public void testMath679_MaxRows() {
    new OpenMapRealMatrix(Integer.MAX_VALUE, 3);
}