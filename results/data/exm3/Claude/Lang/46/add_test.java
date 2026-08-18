// org/apache/commons/lang/StringEscapeUtilsTest.java
public void testEscapeJavaScriptWithSlash() {
    final String input = "String with a slash (/) in it";
    final String expected = "String with a slash (\\/) in it";
    final String actual = StringEscapeUtils.escapeJavaScript(input);
    assertEquals(expected, actual);
}