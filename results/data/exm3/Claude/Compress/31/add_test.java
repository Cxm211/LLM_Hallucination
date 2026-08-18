// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
@Test
public void testParseOctalAllSpaces() throws Exception {
    byte[] buffer = "    ".getBytes(CharsetNames.UTF_8); // All spaces
    try {
        TarUtils.parseOctal(buffer, 0, buffer.length);
        fail("Expected IllegalArgumentException - no octal digits");
    } catch (IllegalArgumentException expected) {
    }
}