// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseOctalValidDigitsAfterSpaces() throws Exception {
        byte[] buffer = new byte[]{' ', '7', '7', '7', 0};
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(511, value);
    }