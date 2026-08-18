// org/apache/commons/compress/archivers/tar/TarArchiveOutputStreamTest.java
@Test
    public void testWritePartialThenFill() throws Exception {
        File f = File.createTempFile("commons-compress-tarpartial", ".tar");
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);
        ArchiveOutputStream tarOut = new ArchiveStreamFactory()
            .createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(512);
        tarOut.putArchiveEntry(entry);
        // Write first partial chunk
        byte[] part1 = new byte[100];
        tarOut.write(part1, 0, part1.length);
        // Write remaining to fill a record
        byte[] part2 = new byte[412];
        tarOut.write(part2, 0, part2.length);
        tarOut.closeArchiveEntry();
        tarOut.close();
        assertEquals(f.length(), tarOut.getBytesWritten());
    }
