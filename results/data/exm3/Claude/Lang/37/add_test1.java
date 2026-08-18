// org/apache/commons/lang3/ArrayUtilsAddTest.java
public void testAddAllIncompatiblePrimitiveWrappers() {
    try {
        Integer[] result = ArrayUtils.addAll(new Integer[]{Integer.valueOf(1)}, new Double[]{Double.valueOf(2.5)});
        fail("Should have generated IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
}