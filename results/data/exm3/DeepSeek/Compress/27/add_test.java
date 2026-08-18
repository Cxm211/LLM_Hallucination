// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalLeadingSpacesThenNul() throws Exception {
    byte[] buffer1 = {' ', ' ', 0, ' '};
    assertEquals(0L, TarUtils.parseOctal(buffer1, 0, buffer1.length));
    byte[] buffer2 = {' ', 0, '1', '2'};
    assertEquals(0L, TarUtils.parseOctal(buffer2, 0, buffer2.length));
    byte[] buffer3 = {' ', ' ', 0};
    assertEquals(0L, TarUtils.parseOctal(buffer3, 0, buffer3.length));
}
