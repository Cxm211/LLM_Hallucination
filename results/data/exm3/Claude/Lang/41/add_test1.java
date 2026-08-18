// org/apache/commons/lang/ClassUtilsTest.java
public void test_getPackageName_MultiDimensionalArrays() {
    assertEquals("java.util", ClassUtils.getPackageName(Map.Entry[][].class));
    assertEquals("", ClassUtils.getPackageName(int[][].class));
    assertEquals("", ClassUtils.getPackageName(boolean[][][].class));
}