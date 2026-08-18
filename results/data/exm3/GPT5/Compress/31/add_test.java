// org/apache/commons/compress/archivers/tar/TarUtilsTest.java::testParseOctalEmbeddedNulAfterSpaces
@Test
    public void testParseOctalEmbeddedNulAfterSpaces() throws Exception {
        byte[] buffer = " \0007 ".getBytes(CharsetNames.UTF_8);
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException - embedded NUL after spaces");
        } catch (IllegalArgumentException expected) {
        }
    }