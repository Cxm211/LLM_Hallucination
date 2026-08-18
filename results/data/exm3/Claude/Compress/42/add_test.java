// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
@Test
public void isUnixSymlinkIsTrueWhenOnlySymlinkFlagIsSet() throws Exception {
    ZipArchiveEntry ze = new ZipArchiveEntry("test");
    ze.setUnixMode(UnixStat.LINK_FLAG);
    assertTrue(ze.isUnixSymlink());
}