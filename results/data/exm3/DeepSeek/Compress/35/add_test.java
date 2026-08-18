// org/apache/commons/compress/DetectArchiverTestCase.java
@Test
    public void testVerifyCheckSumWithSpaceInChecksumField() {
        byte[] header = new byte[512];
        int offset = TarUtils.CHKSUM_OFFSET;
        header[offset]   = '0';
        header[offset+1] = '0';
        header[offset+2] = '0';
        header[offset+3] = ' ';
        header[offset+4] = '4';
        header[offset+5] = '0';
        header[offset+6] = '0';
        header[offset+7] = ' ';
        assertTrue(TarUtils.verifyCheckSum(header));
    }
