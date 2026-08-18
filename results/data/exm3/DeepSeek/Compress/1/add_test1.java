// org/apache/commons/compress/archivers/CpioTestCase.java
public void testCloseWithExceptionOnInputStream() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream("cpio", baos);
        aos.putArchiveEntry(new CpioArchiveEntry("test", 0));
        aos.closeArchiveEntry();
        aos.close();
        byte[] archive = baos.toByteArray();
        InputStream in = new ByteArrayInputStream(archive) {
            @Override
            public void close() throws IOException {
                throw new IOException("Simulated exception on close");
            }
        };
        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream("cpio", in);
        assertNotNull(ais.getNextEntry());
        try {
            ais.close();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        ais.close(); // second close should not throw
    }
