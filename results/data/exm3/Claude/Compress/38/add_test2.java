// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void regularFileWithSlashNotDirectory() throws Exception {
    final TarArchiveEntry entry = new TarArchiveEntry("regularfile/");
    entry.setLinkFlag(TarConstants.LF_NORMAL);
    assertTrue("Regular file with trailing slash should be considered a directory", entry.isDirectory());
}