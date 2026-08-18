// org/apache/commons/compress/archivers/tar/TarArchiveOutputStreamTest.java
@Test
    public void testWriteExactRecord() throws Exception {
        File f = File.createTempFile("commons-compress-tarexact", ".tar");
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);
        ArchiveOutputStream tarOut = new ArchiveStreamFactory()
            .createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(512);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[512];
        tarOut.write(data, 0, data.length);
        tarOut.closeArchiveEntry();
        tarOut.close();
        assertEquals(f.length(), tarOut.getBytesWritten());
    }
