// org/apache/commons/compress/DetectArchiverTestCase.java
@Test
    public void testVerifyCheckSumWithNonDigitInChecksumField() {
        byte[] header = new byte[512];
        int offset = TarUtils.CHKSUM_OFFSET;
        header[offset]   = '4';
        header[offset+1] = 'x';
        header[offset+2] = '0';
        header[offset+3] = '0';
        for (int i = offset+4; i < offset+TarUtils.CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        assertTrue(TarUtils.verifyCheckSum(header));
    }
