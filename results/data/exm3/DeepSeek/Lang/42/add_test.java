// org/apache/commons/lang/StringEscapeUtilsTest.java
public void testEscapeHtmlMixed() throws java.io.UnsupportedEncodingException {
        String input = "A&<>\u20AC\uD83D\uDE00";
        String escaped = StringEscapeUtils.escapeHtml(input);
        String expected = "A&amp;&lt;&gt;&#8364;&#128512;";
        assertEquals("Mixed characters escaping failed", expected, escaped);
    }
