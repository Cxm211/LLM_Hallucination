// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalAdditional() throws Exception {
    byte[] buffer;
    buffer = "123".getBytes("UTF-8");
    try {
        TarUtils.parseOctal(buffer, 0, buffer.length);
        fail("Expected IllegalArgumentException for no terminator");
    } catch (IllegalArgumentException expected) {
    }
    buffer = new byte[]{'1', '2', 0, '3', 0};
    try {
        TarUtils.parseOctal(buffer, 0, buffer.length);
        fail("Expected IllegalArgumentException for embedded NUL");
    } catch (IllegalArgumentException expected) {
    }
}
