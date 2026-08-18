// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void testConstructorWithEncodingAndFlags() throws Exception {
    ZipArchiveInputStream in = new ZipArchiveInputStream(
        new FileInputStream(getFile("COMPRESS-264.zip")),
        "UTF-8",
        true,
        true
    );
    
    try {
        ZipArchiveEntry ze = in.getNextZipEntry();
        assertNotNull(ze);
        assertEquals(5, ze.getSize());
        assertArrayEquals(new byte[] {'d', 'a', 't', 'a', '\n'},
                          IOUtils.toByteArray(in));
    } finally {
        in.close();
    }
}