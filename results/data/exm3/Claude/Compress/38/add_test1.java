// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void globalPaxHeaderNotConsideredDirectory() throws Exception {
    final TarArchiveEntry entry = new TarArchiveEntry("GlobalHead/");
    entry.setLinkFlag(TarConstants.LF_GNUTYPE_LONGNAME);
    assertFalse("Global Pax header should not be considered a directory even with slash", entry.isDirectory());
}