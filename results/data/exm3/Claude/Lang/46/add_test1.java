// org/apache/commons/lang/StringEscapeUtilsTest.java
public void testEscapeJavaWithSingleQuote() {
    final String input = "It's a test";
    final String expected = "It's a test";
    final String actual = StringEscapeUtils.escapeJava(input);
    assertEquals(expected, actual);
}