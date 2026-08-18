// org/apache/commons/compress/archivers/TarTestCase.java
public void testGNULongNameEntry() throws Exception {
    // Create a tar in memory
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 200; i++) {
        sb.append('a');
    }
    String longName = sb.toString();
    TarArchiveEntry entry = new TarArchiveEntry(longName);
    entry.setSize(0);
    tos.putArchiveEntry(entry);
    tos.closeArchiveEntry();
    tos.finish();
    tos.close();
    
    byte[] data = bos.toByteArray();
    TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(data));
    TarArchiveEntry readEntry = tis.getNextTarEntry();
    assertNotNull(readEntry);
    assertEquals(longName, readEntry.getName());
    assertNull(tis.getNextTarEntry());
    tis.close();
}
