// org/apache/commons/compress/DetectArchiverTestCase.java::testSevenDigitTarChecksum
@Test
public void testSevenDigitTarChecksum() {
    // Construct a minimal tar header with checksum field set to 7 octal digits representing 256 (0400)
    // Tar header is 512 bytes; checksum field starts at offset 148 and is 8 bytes long.
    byte[] header = new byte[512];
    int CHKSUM_OFFSET = 148;
    // Place seven octal digits "0000400" then a NUL terminator
    byte[] octal = new byte[] { '0','0','0','0','4','0','0', 0 };
    System.arraycopy(octal, 0, header, CHKSUM_OFFSET, octal.length);
    // verifyCheckSum should return true when parsing 7-digit checksums
    assertTrue(TarUtils.verifyCheckSum(header));
}
