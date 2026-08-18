// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalAllZeros() throws Exception {
    byte[] buffer = "00000".getBytes("UTF-8"); // All zeros
    try {
        TarUtils.parseOctal(buffer, 0, buffer.length);
        fail("Expected IllegalArgumentException - all zeros");
    } catch (IllegalArgumentException expected) {
    }
}