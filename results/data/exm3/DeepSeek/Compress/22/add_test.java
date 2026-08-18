// org/apache/commons/compress/compressors/bzip2/PythonTruncatedBzip2Test.java
@Test
    public void testReadCompleteStream() throws IOException {
        // Create a complete BZip2 compressed stream from TEXT
        byte[] inputData = TEXT.getBytes();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream bzOut = 
                new org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream(baos)) {
            bzOut.write(inputData);
        }
        byte[] compressedData = baos.toByteArray();
        java.io.InputStream in = new java.io.ByteArrayInputStream(compressedData);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in, false);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = bzIn.read(buffer)) != -1) {
            // read all data
        }
        // After EOF, read should return -1
        int afterRead = bzIn.read();
        org.junit.Assert.assertEquals(-1, afterRead);
    }
