// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
@Test
    public void isUnixSymlinkIsFalseForCombinedSymlinkAndFifo() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setUnixMode(UnixStat.LINK_FLAG | UnixStat.FIFO_FLAG);
        assertFalse(entry.isUnixSymlink());
    }
