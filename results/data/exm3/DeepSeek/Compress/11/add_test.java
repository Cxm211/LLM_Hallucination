// org/apache/commons/compress/archivers/ArchiveStreamFactoryTest.java
@Test
    public void shortZeroFileIsNoTAR() throws Exception {
        try {
            new ArchiveStreamFactory()
                .createArchiveInputStream(new ByteArrayInputStream(new byte[10]));
            fail("created an input stream for a non-archive");
        } catch (ArchiveException ae) {
            assertTrue(ae.getMessage().startsWith("No Archiver found"));
        }
    }
