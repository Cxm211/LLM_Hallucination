// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
@Test
public void isUnixSymlinkIsFalseForRegularFile() throws Exception {
    ZipArchiveEntry ze = new ZipArchiveEntry("test");
    ze.setUnixMode(UnixStat.FILE_FLAG | 0644);
    assertFalse(ze.isUnixSymlink());
}