// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalAllSpacesAndNuls() throws Exception {
    byte[] buffer = new byte[]{' ', ' ', 0, 0};
    long value = TarUtils.parseOctal(buffer, 0, buffer.length);
    assertEquals(0L, value);
}