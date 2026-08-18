// org/apache/commons/lang3/ArrayUtilsAddTest.java
public void testAddNullArrayNonNullElement() {
    String[] stringArray = null;
    String aString = "test";
    String[] result = ArrayUtils.add(stringArray, aString);
    assertNotNull(result);
    assertEquals(1, result.length);
    assertEquals("test", result[0]);
}