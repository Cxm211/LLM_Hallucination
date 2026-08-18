// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
@Test
    public void isUnixSymlinkIsFalseIfLinkAndFileFlagsSet() {
        org.apache.commons.compress.archivers.zip.ZipArchiveEntry ze = new org.apache.commons.compress.archivers.zip.ZipArchiveEntry("name");
        ze.setUnixMode(org.apache.commons.compress.archivers.zip.UnixStat.LINK_FLAG
                | org.apache.commons.compress.archivers.zip.UnixStat.FILE_FLAG
                | org.apache.commons.compress.archivers.zip.UnixStat.DEFAULT_FILE_PERM);
        assertFalse(ze.isUnixSymlink());
    }