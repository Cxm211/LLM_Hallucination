// org/apache/commons/compress/archivers/tar/TarArchiveOutputStreamTest.java
public void testCountWithSmallWrites() throws Exception {
    File f = File.createTempFile("commons-compress-tarcount-small", ".tar");
    f.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(f);

    ArchiveOutputStream tarOut = new ArchiveStreamFactory()
        .createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);

    File file1 = getFile("test1.xml");
    TarArchiveEntry sEntry = new TarArchiveEntry(file1);
    tarOut.putArchiveEntry(sEntry);

    FileInputStream in = new FileInputStream(file1);
    byte[] buf = new byte[32];

    int read = 0;
    while ((read = in.read(buf)) > 0) {
        tarOut.write(buf, 0, read);
    }

    in.close();
    tarOut.closeArchiveEntry();
    tarOut.close();

    assertEquals(f.length(), tarOut.getBytesWritten());
}