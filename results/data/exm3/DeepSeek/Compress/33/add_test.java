// org/apache/commons/compress/compressors/DetectCompressorTestCase.java
@Test
    public void testDeflateDetection() throws Exception {
        // Simulate a zlib header: first byte 0x78, second byte 0x9C
        byte[] deflateSignature = new byte[] { 0x78, 0x9C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        ByteArrayInputStream in = new ByteArrayInputStream(deflateSignature);
        CompressorInputStream cis = factory.createCompressorInputStream(in);
        assertNotNull(cis);
        assertTrue(cis instanceof DeflateCompressorInputStream);
    }
