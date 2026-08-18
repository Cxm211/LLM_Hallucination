// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testUtf8FileRoundtripImplicitUnicodeExtra
public void testCloseClosesStreamOnFinishException() throws IOException {
        class TrackingOS extends java.io.OutputStream {
            boolean closed;
            public void write(int b) throws IOException { /* no-op to avoid early failure */ }
            public void close() throws IOException { closed = true; }
        }
        class BrokenZipArchiveOutputStream extends org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream {
            BrokenZipArchiveOutputStream(java.io.OutputStream out) { super(out); }
            public void finish() throws IOException { throw new IOException("boom"); }
        }
        TrackingOS tos = new TrackingOS();
        BrokenZipArchiveOutputStream zaos = new BrokenZipArchiveOutputStream(tos);
        try {
            try {
                zaos.close();
                fail("Expected IOException");
            } catch (IOException ex) {
                // expected
            }
            assertTrue("underlying stream must be closed when finish() fails", tos.closed);
        } finally {
            // nothing else to close
        }
    }