// org/apache/commons/compress/archivers/CpioTestCase.java
public void testCloseWithExceptionOnOutputStream() throws Exception {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // do nothing
            }
            @Override
            public void close() throws IOException {
                throw new IOException("Simulated exception on close");
            }
        };
        ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("cpio", out);
        os.putArchiveEntry(new CpioArchiveEntry("dummy", 0));
        os.closeArchiveEntry();
        try {
            os.close();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        os.close(); // second close should not throw
    }
