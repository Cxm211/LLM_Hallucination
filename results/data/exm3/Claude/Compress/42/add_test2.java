// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
@Test
public void isUnixSymlinkIsFalseForDirectory() throws Exception {
    ZipArchiveEntry ze = new ZipArchiveEntry("test");
    ze.setUnixMode(UnixStat.DIR_FLAG | 0755);
    assertFalse(ze.isUnixSymlink());
}