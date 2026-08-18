// org/apache/commons/compress/compressors/bzip2/BZip2CompressorInputStreamTest.java
@Test
    public void readWithOverflowShouldThrow() throws Exception {
        // Create a small compressed data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BZip2CompressorOutputStream bzipOut = new BZip2CompressorOutputStream(baos);
        bzipOut.write(1);
        bzipOut.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        BZip2CompressorInputStream bzipIn = new BZip2CompressorInputStream(bais);
        byte[] buffer = new byte[100];
        // offs=1, len=Integer.MAX_VALUE causes overflow, should throw IndexOutOfBoundsException
        try {
            bzipIn.read(buffer, 1, Integer.MAX_VALUE);
            Assert.fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        bzipIn.close();
    }
