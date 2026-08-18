// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalAllSpaces() throws Exception {
    byte[] buffer = "     ".getBytes("UTF-8"); // All spaces
    try {
        TarUtils.parseOctal(buffer, 0, buffer.length);
        fail("Expected IllegalArgumentException - all spaces");
    } catch (IllegalArgumentException expected) {
    }
}