// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
    public void testParseOctalNoTerminator() {
        byte[] buffer = {' ', '1', '2'};
        long result = TarUtils.parseOctal(buffer, 0, 3);
        assertEquals(10L, result);
    }
