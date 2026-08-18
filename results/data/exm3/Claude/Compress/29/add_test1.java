// org/apache/commons/compress/archivers/ArchiveStreamFactoryTest.java
@Test
public void testJarEncodingOutputStreamNullEncoding() throws Exception {
    ArchiveStreamFactory fac = new ArchiveStreamFactory();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ArchiveOutputStream aos = fac.createArchiveOutputStream(ArchiveStreamFactory.JAR, baos);
    String encoding = getField(aos, "zipEncoding");
    assertNotNull(encoding);
}