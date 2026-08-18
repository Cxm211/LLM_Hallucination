// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalLeadingSpacesWithTrailing() throws Exception {
    byte[] buffer = new byte[]{' ', ' ', '7', '7', '7', ' '};
    long value = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(0777L, value);
}