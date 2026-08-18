// org/apache/commons/compress/archivers/tar/TarArchiveOutputStreamTest.java
public void testWriteVeryLongNonAsciiDirectoryNamePosixMode() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("ab");
        }
        sb.append("/");
        String n = sb.toString();
        TarArchiveEntry t = new TarArchiveEntry(n);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);
        tos.putArchiveEntry(t);
        tos.closeArchiveEntry();
        tos.close();
        byte[] data = bos.toByteArray();
        TarArchiveInputStream tin =
            new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertEquals(n, e.getName());
        assertTrue(e.isDirectory());
        tin.close();
    }