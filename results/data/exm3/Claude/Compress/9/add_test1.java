// org/apache/commons/compress/archivers/tar/TarArchiveOutputStreamTest.java
public void testCountWithSingleByteWrites() throws Exception {
    File f = File.createTempFile("commons-compress-tarcount-single", ".tar");
    f.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(f);

    ArchiveOutputStream tarOut = new ArchiveStreamFactory()
        .createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);

    File file1 = getFile("test1.xml");
    TarArchiveEntry sEntry = new TarArchiveEntry(file1);
    tarOut.putArchiveEntry(sEntry);

    FileInputStream in = new FileInputStream(file1);
    int byteRead;
    while ((byteRead = in.read()) != -1) {
        tarOut.write(byteRead);
    }

    in.close();
    tarOut.closeArchiveEntry();
    tarOut.close();

    assertEquals(f.length(), tarOut.getBytesWritten());
}