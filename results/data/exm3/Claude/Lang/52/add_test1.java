// org/apache/commons/lang/StringEscapeUtilsTest.java
public void testEscapeJavaScriptMixedContent() {
    assertEquals("text\\/more", StringEscapeUtils.escapeJavaScript("text/more"));
    assertEquals("\\/start", StringEscapeUtils.escapeJavaScript("/start"));
    assertEquals("end\\/", StringEscapeUtils.escapeJavaScript("end/"));
}