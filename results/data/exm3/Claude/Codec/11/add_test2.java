// org/apache/commons/codec/net/QuotedPrintableCodecTest.java
@Test
public void testOnlyWhitespace() throws Exception {
    final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
    
    // Test string with only trailing whitespace
    String plain = "   ";
    String encoded = qpcodec.encode(plain);
    
    // All whitespace should be encoded as it's trailing
    assertEquals("=20=20=20", encoded);
    
    // Verify decode
    assertEquals(plain, qpcodec.decode(encoded));
}