// org/apache/commons/lang3/ArrayUtilsAddTest.java
public void testAddWithIndexNullArrayNonNullElement() {
    Integer[] intArray = null;
    Integer element = 42;
    Integer[] result = ArrayUtils.add(intArray, 0, element);
    assertNotNull(result);
    assertEquals(1, result.length);
    assertEquals(Integer.valueOf(42), result[0]);
}