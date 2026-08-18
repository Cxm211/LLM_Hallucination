// org/apache/commons/compress/DetectArchiverTestCase.java
@Test
public void testVerifyCheckSumWithZeroChecksum() throws Exception {
    byte[] header = new byte[512];
    for (int i = 0; i < header.length; i++) {
        header[i] = 0;
    }
    int CHKSUM_OFFSET = 148;
    int CHKSUMLEN = 8;
    for (int i = CHKSUM_OFFSET; i < CHKSUM_OFFSET + CHKSUMLEN; i++) {
        header[i] = ' ';
    }
    boolean result = TarArchiveInputStream.verifyCheckSum(header);
    assertTrue(result);
}