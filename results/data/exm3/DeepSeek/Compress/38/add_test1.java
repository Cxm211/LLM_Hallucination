// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
    public void symlinkFileIsNotDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("test");
        Path targetDir = tempDir.resolve("target");
        Files.createDirectory(targetDir);
        Path link = tempDir.resolve("link");
        Files.createSymbolicLink(link, targetDir);
        TarArchiveEntry entry = new TarArchiveEntry(link.toFile());
        assertFalse(entry.isDirectory());
        Files.deleteIfExists(link);
        Files.deleteIfExists(targetDir);
        Files.deleteIfExists(tempDir);
    }
