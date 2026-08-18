// org/apache/commons/compress/archivers/tar/TarArchiveOutputStreamTest.java
@Test
    public void testWriteSingleByte() throws Exception {
        File f = File.createTempFile("commons-compress-tarsingle", ".tar");
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);
        ArchiveOutputStream tarOut = new ArchiveStreamFactory()
            .createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(1);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{42}, 0, 1);
        tarOut.closeArchiveEntry();
        tarOut.close();
        assertEquals(f.length(), tarOut.getBytesWritten());
    }
