// org/apache/commons/lang/StringEscapeUtilsTest.java
public void testEscapeJavaWithSlashAndBackslash() {
        final String input = "foo/bar\\baz";
        final String expected = "foo/bar\\\\baz";
        final String actual = StringEscapeUtils.escapeJava(input);
        assertEquals(expected, actual);
    }
