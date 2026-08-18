// org/apache/commons/codec/net/QuotedPrintableCodecTest.java
@Test
public void testMultipleConsecutiveSpecialChars() throws Exception {
    final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
    
    // Test multiple special characters in sequence
    String plain = "Test===data";
    String encoded = qpcodec.encode(plain);
    
    // All equals signs should be encoded
    assertTrue(encoded.contains("=3D"));
    
    // Verify round trip
    assertEquals(plain, qpcodec.decode(encoded));
}