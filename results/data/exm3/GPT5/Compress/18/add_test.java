// org/apache/commons/compress/archivers/tar/TarArchiveOutputStreamTest.java
public void testWriteNonAsciiLongDirectoryNamePosixMode() throws Exception {
        String base = "f\u00f6\u00f6";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) { sb.append(base); }
        String n = sb.append("/").toString();
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