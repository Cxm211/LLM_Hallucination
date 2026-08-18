// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalNoTrailerWithLeadingSpace() throws Exception {
        byte[] buffer = " 7".getBytes("UTF-8"); // no trailer after digit
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException - no trailer after leading space");
        } catch (IllegalArgumentException expected) {
        }
    }