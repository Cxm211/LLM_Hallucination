// org/apache/commons/lang3/ArrayUtilsAddTest.java
public void testAddNonNullArrayNullElement() {
    String[] stringArray = new String[]{"existing"};
    String aString = null;
    String[] result = ArrayUtils.add(stringArray, aString);
    assertNotNull(result);
    assertEquals(2, result.length);
    assertEquals("existing", result[0]);
    assertNull(result[1]);
}