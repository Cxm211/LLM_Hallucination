// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
    public void testParseOctalSingleDigit() {
        byte[] buffer = {'7'};
        long result = TarUtils.parseOctal(buffer, 0, 1);
        assertEquals(7L, result);
    }
