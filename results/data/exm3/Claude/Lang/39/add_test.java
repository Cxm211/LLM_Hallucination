// org/apache/commons/lang3/StringUtilsTest.java
public void testReplaceEach_NullInMiddleOfArrays() {
    assertEquals("cdc", StringUtils.replaceEach("aba", new String[]{"a", null, "b"}, new String[]{"c", "x", "d"}));
    assertEquals("aba", StringUtils.replaceEach("aba", new String[]{null, "x", "y"}, new String[]{"p", "q", "r"}));
}