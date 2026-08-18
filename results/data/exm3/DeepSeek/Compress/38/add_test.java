// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
    public void symlinkWithTrailingSlashIsNotDirectory() {
        TarArchiveEntry entry = new TarArchiveEntry("linkto/", TarConstants.LF_SYMLINK);
        assertFalse(entry.isDirectory());
    }
