// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalWithMultipleLeadingSpaces() throws Exception {
        byte[] buffer = new byte[]{' ', ' ', '1', '2', '3', ' '};
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(83, value);
    }