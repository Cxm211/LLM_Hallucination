// org/apache/commons/lang/StringEscapeUtilsTest.java::testEscapeJavaWithSlashUsingWriter
public void testEscapeJavaWithSlashUsingWriter() throws java.io.IOException {
        final String input = "String with a slash (/) in it";
        final java.io.StringWriter sw = new java.io.StringWriter();
        StringEscapeUtils.escapeJava(sw, input);
        assertEquals(input, sw.toString());
    }