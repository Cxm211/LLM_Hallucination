// org/apache/commons/lang/StringEscapeUtilsTest.java
public void testEscapeJavaScriptForwardSlash() {
    assertEquals("\\/", StringEscapeUtils.escapeJavaScript("/"));
    assertEquals("a\\/b", StringEscapeUtils.escapeJavaScript("a/b"));
    assertEquals("\\/\\/", StringEscapeUtils.escapeJavaScript("//"));
}