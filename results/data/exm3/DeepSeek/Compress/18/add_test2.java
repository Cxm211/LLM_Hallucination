// org/apache/commons/compress/archivers/tar/TarArchiveOutputStreamTest.java
public void testWriteDeepNonAsciiDirectoryName() throws Exception {
        String n = "a/b/c/\u00f6\u00f6/\u00e4\u00e4/";
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
