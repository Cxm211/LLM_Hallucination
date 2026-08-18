// org/apache/commons/codec/net/QuotedPrintableCodecTest.java
@Test
public void testSoftLineBreakDecodeLFOnly() throws Exception {
    String qpdata = "a=\nb";
    String expected = "ab";
    QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
    assertEquals(expected, qpcodec.decode(qpdata));
}