// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
    public void testParseOctalLengthZero() {
        byte[] buffer = new byte[10];
        long result = TarUtils.parseOctal(buffer, 0, 0);
        assertEquals(0L, result);
    }
