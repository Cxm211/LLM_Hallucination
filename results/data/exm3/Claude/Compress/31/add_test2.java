// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
@Test
public void testParseOctalLeadingSpacesTrailingSpaces() throws Exception {
    byte[] buffer = "   \0\0\0".getBytes(CharsetNames.UTF_8); // Leading spaces, trailing NULs
    try {
        TarUtils.parseOctal(buffer, 0, buffer.length);
        fail("Expected IllegalArgumentException - no octal digits");
    } catch (IllegalArgumentException expected) {
    }
}