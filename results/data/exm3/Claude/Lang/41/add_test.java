// org/apache/commons/lang/ClassUtilsTest.java
public void test_getShortClassName_MultiDimensionalPrimitiveArrays() {
    assertEquals("boolean[][]", ClassUtils.getShortClassName(boolean[][].class));
    assertEquals("int[][][]", ClassUtils.getShortClassName(int[][][].class));
    assertEquals("double[][][][]", ClassUtils.getShortClassName(double[][][][].class));
}