// org/apache/commons/compress/archivers/ArTestCase.java
public void testArOddLength() throws Exception {
    final File output = new File(dir, "odd.ar");
    final byte[] data = new byte[] { 65 };
    final OutputStream out = new FileOutputStream(output);
    final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("ar", out);
    os.putArchiveEntry(new ArArchiveEntry("odd.txt", data.length));
    os.write(data);
    os.closeArchiveEntry();
    os.close();
    out.close();

    final InputStream is = new FileInputStream(output);
    final ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is));
    final ArArchiveEntry entry = (ArArchiveEntry) ais.getNextEntry();
    assertNotNull(entry);
    assertEquals("odd.txt", entry.getName());
    assertEquals(data.length, entry.getLength());
    final byte[] readData = new byte[data.length];
    final int read = IOUtils.read(ais, readData);
    assertEquals(data.length, read);
    assertArrayEquals(data, readData);
    assertNull(ais.getNextEntry());
    ais.close();
    is.close();
}
