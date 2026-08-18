// org/apache/commons/codec/net/QuotedPrintableCodecTest.java
@Test
public void testLongLineWithoutSpecialChars() throws Exception {
    final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
    
    // Test line longer than 76 characters with only printable chars
    String plain = "This is a very long line that contains only printable characters and should trigger a soft line break";
    String encoded = qpcodec.encode(plain);
    
    // Verify soft line break is inserted
    assertTrue(encoded.contains("=\r\n"));
    
    // Verify it decodes back correctly
    assertEquals(plain, qpcodec.decode(encoded));
}