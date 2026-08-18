// org/apache/commons/lang/ClassUtilsTest.java
public void test_getShortClassName_InnerClassArray() {
    assertEquals("Map.Entry[][][]", ClassUtils.getShortClassName(Map.Entry[][][].class));
}