// org/apache/commons/compress/archivers/zip/ZipArchiveInputStreamTest.java
@Test
public void testConstructorDoesNotThrowOnEmptyStream() throws Exception {
    ZipArchiveInputStream z = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, false);
    z.close();
}
