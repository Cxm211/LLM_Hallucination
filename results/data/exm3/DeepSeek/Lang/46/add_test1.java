// org/apache/commons/lang/StringEscapeUtilsTest.java
public void testEscapeJavaWriterWithSlash() throws IOException {
        final String input = "test/slash";
        final StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, input);
        final String actual = writer.toString();
        assertEquals(input, actual);
    }
