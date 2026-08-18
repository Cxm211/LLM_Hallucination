// org/apache/commons/lang/StringEscapeUtilsTest.java
public void testEscapeJavaScriptVariousCharacters() {
        // Test DEL character (0x7f) which was previously not escaped
        assertEquals("\\u007f", StringEscapeUtils.escapeJavaScript("\u007f"));
        // Test other control character not specially handled
        assertEquals("\\u0001", StringEscapeUtils.escapeJavaScript("\u0001"));
        // Test Unicode characters in different ranges
        assertEquals("\\u0080", StringEscapeUtils.escapeJavaScript("\u0080"));
        assertEquals("\\u0100", StringEscapeUtils.escapeJavaScript("\u0100"));
        assertEquals("\\u1000", StringEscapeUtils.escapeJavaScript("\u1000"));
        // Test backslash escaping
        assertEquals("\\\\", StringEscapeUtils.escapeJavaScript("\\"));
        // Test single and double quotes (already covered but ensure still correct)
        assertEquals("\\'", StringEscapeUtils.escapeJavaScript("'"));
        assertEquals("\\\"", StringEscapeUtils.escapeJavaScript("\""));
    }
