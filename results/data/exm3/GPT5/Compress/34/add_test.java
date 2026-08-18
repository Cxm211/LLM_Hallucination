// org/apache/commons/compress/archivers/zip/X7875_NewUnixTest.java::testParseReparse
@Test
public void testCentralDirectoryLengthMatchesDataLength() throws ZipException {
    final byte[] SPURIOUS_ZEROES_1 = {1, 4, -1, 0, 0, 0, 4, -128, 0, 0, 0};
    xf.parseFromLocalFileData(SPURIOUS_ZEROES_1, 0, SPURIOUS_ZEROES_1.length);
    assertEquals(xf.getCentralDirectoryData().length, xf.getCentralDirectoryLength().getValue());
}
