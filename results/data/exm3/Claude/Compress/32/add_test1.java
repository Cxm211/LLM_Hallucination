// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
public void shouldReadBigUidAndGid() throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
    tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
    TarArchiveEntry t = new TarArchiveEntry("name");
    t.setUserId(3000000000l);
    t.setGroupId(3500000000l);
    t.setSize(1);
    tos.putArchiveEntry(t);
    tos.write(30);
    tos.closeArchiveEntry();
    tos.close();
    byte[] data = bos.toByteArray();
    ByteArrayInputStream bis = new ByteArrayInputStream(data);
    TarArchiveInputStream tis = new TarArchiveInputStream(bis);
    t = tis.getNextTarEntry();
    assertEquals(3000000000l, t.getLongUserId());
    assertEquals(3500000000l, t.getLongGroupId());
    tis.close();
}