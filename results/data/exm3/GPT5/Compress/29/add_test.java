// org/apache/commons/compress/archivers/ArchiveStreamFactoryTest.java::testTarEncodingChecksumAutodetect
@Test
public void testTarEncodingChecksumAutodetect() throws Exception {
    final String expected = "UTF-16";
    ArchiveStreamFactory fac = new ArchiveStreamFactory(expected);

    byte[] header = new byte[512];
    // name
    byte[] name = "test.txt".getBytes("US-ASCII");
    System.arraycopy(name, 0, header, 0, name.length);
    // mode
    byte[] mode = String.format("%07o\0", 0777).getBytes("US-ASCII");
    System.arraycopy(mode, 0, header, 100, mode.length);
    // uid, gid
    byte[] ug = String.format("%07o\0", 0).getBytes("US-ASCII");
    System.arraycopy(ug, 0, header, 108, ug.length);
    System.arraycopy(ug, 0, header, 116, ug.length);
    // size 0
    byte[] size = String.format("%011o\0", 0).getBytes("US-ASCII");
    System.arraycopy(size, 0, header, 124, size.length);
    // mtime
    byte[] mtime = String.format("%011o\0", 0).getBytes("US-ASCII");
    System.arraycopy(mtime, 0, header, 136, mtime.length);
    // chksum field set to spaces for calculation
    for (int i = 148; i < 156; i++) { header[i] = 0x20; }
    // typeflag '0'
    header[156] = '0';
    // old tar: leave magic zeroed so TarArchiveInputStream.matches is conservative

    long sum = 0;
    for (int i = 0; i < 512; i++) { sum += (header[i] & 0xFF); }
    String chk = String.format("%06o\0 ", sum);
    byte[] chkBytes = chk.getBytes("US-ASCII");
    System.arraycopy(chkBytes, 0, header, 148, chkBytes.length);

    // complete stream: header + two zero blocks end of archive
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(header);
    baos.write(new byte[1024]);
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    BufferedInputStream bin = new BufferedInputStream(bais);

    ArchiveInputStream ais = fac.createArchiveInputStream(bin);
    String field = getField(ais, "zipEncoding");
    assertTrue(eq(expected, field));
}
