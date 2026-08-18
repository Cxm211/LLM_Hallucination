// org/apache/commons/compress/compressors/DetectCompressorTestCase.java
@Test
public void testFramedSnappyDetection() throws Exception {
    CompressorInputStream snappy = getStreamFor("bla.tar.sz");
    assertNotNull(snappy);
    assertTrue(snappy instanceof FramedSnappyCompressorInputStream);
}