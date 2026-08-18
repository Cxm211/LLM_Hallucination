// org/apache/commons/lang/StringEscapeUtilsTest.java
public void testEscapeJavaScriptWithSingleQuote() {
    final String input = "It's a test";
    final String expected = "It\\'s a test";
    final String actual = StringEscapeUtils.escapeJavaScript(input);
    assertEquals(expected, actual);
}