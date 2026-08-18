// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java::workaroundForBrokenTimeHeader
@Test
public void parseOctalAcceptsMultipleTrailingNuls() {
    byte[] buf = new byte[] { ' ', '1', '2', '3', 0, 0, 0, 0 };
    assertEquals(83L, org.apache.commons.compress.archivers.tar.TarUtils.parseOctal(buf, 0, buf.length));
}