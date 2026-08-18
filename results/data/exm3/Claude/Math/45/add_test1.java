// org/apache/commons/math/linear/OpenMapRealMatrixTest.java
@Test(expected = NumberIsTooLargeException.class)
public void testMath679_BothLarge() {
    new OpenMapRealMatrix(Integer.MAX_VALUE / 2 + 1, 3);
}