// org/apache/commons/compress/archivers/tar/TarUtilsTest.java::testParseOctal
public void testParseOctalLeadingSpacesNoTrailer() throws Exception {
        byte[] buffer = " 777".getBytes(CharsetNames.UTF_8);
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0777L, value);
    }