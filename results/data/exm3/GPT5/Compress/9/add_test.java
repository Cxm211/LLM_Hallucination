// org/apache/commons/compress/archivers/tar/TarArchiveOutputStreamTest.java::testCountAssembly
public void testCountAssembly() throws Exception {
        File f = File.createTempFile("commons-compress-tarcount-asm", ".tar");
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);

        ArchiveOutputStream tarOut = new ArchiveStreamFactory()
            .createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);

        byte[] content = new byte[512];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) i;
        }

        TarArchiveEntry e = new TarArchiveEntry("entry");
        e.setSize(content.length);
        tarOut.putArchiveEntry(e);

        // Write in two chunks to trigger assembly: 100 + 412 = 512
        tarOut.write(content, 0, 100);
        tarOut.write(content, 100, 412);

        tarOut.closeArchiveEntry();
        tarOut.close();

        assertEquals(f.length(), tarOut.getBytesWritten());
    }