// org/apache/commons/compress/compressors/DetectCompressorTestCase.java
@Test
public void testZCompressDetection() throws Exception {
    CompressorInputStream z = getStreamFor("bla.tar.Z");
    assertNotNull(z);
    assertTrue(z instanceof ZCompressorInputStream);
}