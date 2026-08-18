// org/apache/commons/lang/ClassUtilsTest.java
public void test_getShortClassName_MultiDimPrimitiveArray() {
        assertEquals("int[][]", ClassUtils.getShortClassName(int[][].class));
    }
