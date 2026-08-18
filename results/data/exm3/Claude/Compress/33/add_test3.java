// org/apache/commons/compress/compressors/DetectCompressorTestCase.java
@Test
public void testLZMADetection() throws Exception {
    if (LZMAUtils.isLZMACompressionAvailable()) {
        CompressorInputStream lzma = getStreamFor("bla.tar.lzma");
        assertNotNull(lzma);
        assertTrue(lzma instanceof LZMACompressorInputStream);
    }
}