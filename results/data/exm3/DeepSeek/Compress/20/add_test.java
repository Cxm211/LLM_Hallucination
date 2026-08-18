// org/apache/commons/compress/archivers/cpio/CpioArchiveInputStreamTest.java
public void testCpioUnarchiveOldBinaryWithTrailer() throws Exception {
    CpioArchiveInputStream in =
        new CpioArchiveInputStream(new FileInputStream(getFile("oldbinary.cpio")));
    CpioArchiveEntry entry = null;
    int count = 0;
    while ((entry = (CpioArchiveEntry) in.getNextEntry()) != null) {
        count++;
    }
    in.close();
    assertEquals(count, 1);
}
