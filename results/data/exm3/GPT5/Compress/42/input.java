// buggy function
    public boolean isUnixSymlink() {
        return (getUnixMode() & UnixStat.LINK_FLAG) == UnixStat.LINK_FLAG;
    }

// trigger testcase
// org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java::isUnixSymlinkIsFalseIfMoreThanOneFlagIsSet
@Test
    public void isUnixSymlinkIsFalseIfMoreThanOneFlagIsSet() throws Exception {
        try (ZipFile zf = new ZipFile(getFile("COMPRESS-379.jar"))) {
            ZipArchiveEntry ze = zf.getEntry("META-INF/maven/");
            assertFalse(ze.isUnixSymlink());
        }
    }
