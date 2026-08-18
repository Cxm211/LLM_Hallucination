// org/apache/commons/compress/compressors/DetectCompressorTestCase.java
@Test
public void testDeflateDetection() throws Exception {
    CompressorInputStream deflate = getStreamFor("bla.tar.deflatez");
    assertNotNull(deflate);
    assertTrue(deflate instanceof DeflateCompressorInputStream);
}