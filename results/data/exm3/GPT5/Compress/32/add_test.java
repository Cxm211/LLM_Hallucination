// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java::shouldReadBigUid
@Test
    public void shouldReadBigUid() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry t = new TarArchiveEntry("name");
        t.setUserId(4294967294L);
        t.setSize(1);
        tos.putArchiveEntry(t);
        tos.write(30);
        tos.closeArchiveEntry();
        tos.close();
        byte[] data = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        t = tis.getNextTarEntry();
        assertEquals(4294967294L, t.getLongUserId());
        tis.close();
    }