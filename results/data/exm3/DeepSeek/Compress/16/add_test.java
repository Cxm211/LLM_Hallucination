// org/apache/commons/compress/archivers/ArchiveStreamFactoryTest.java
@Test
    public void allZeroStreamIsNoTAR() throws Exception {
        InputStream is = new ByteArrayInputStream(new byte[512]);
        try {
            new ArchiveStreamFactory().createArchiveInputStream(is);
            fail("created an input stream for a non-archive");
        } catch (ArchiveException ae) {
            assertTrue(ae.getMessage().startsWith("No Archiver found"));
        }
    }
