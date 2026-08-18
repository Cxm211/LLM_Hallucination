// org/apache/commons/compress/archivers/TarTestCase.java::testCOMPRESS178_Direct
public void testCOMPRESS178_Direct() throws Exception {
        final File input = getFile("COMPRESS-178.tar");
        final InputStream is = new FileInputStream(input);
        final TarArchiveInputStream in = new TarArchiveInputStream(is);
        try {
            in.getNextTarEntry();
            fail("Expected IOException");
        } catch (IOException e) {
            Throwable t = e.getCause();
            assertTrue("Expected cause = IllegalArgumentException", t instanceof IllegalArgumentException);
        } finally {
            in.close();
        }
    }