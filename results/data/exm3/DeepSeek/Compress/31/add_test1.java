// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
@Test
    public void testParseOctalEmbeddedNulAfterSpace() throws Exception {
        byte[] buffer = " \000123".getBytes(CharsetNames.UTF_8);
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }
