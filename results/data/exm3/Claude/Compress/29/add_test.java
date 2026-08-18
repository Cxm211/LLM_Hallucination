// org/apache/commons/compress/archivers/ArchiveStreamFactoryTest.java
@Test
public void testJarEncodingOutputStream() throws Exception {
    ArchiveStreamFactory fac = new ArchiveStreamFactory("UTF-16");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ArchiveOutputStream aos = fac.createArchiveOutputStream(ArchiveStreamFactory.JAR, baos);
    String encoding = getField(aos, "zipEncoding");
    assertEquals("UTF-16", encoding);
}