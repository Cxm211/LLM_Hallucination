// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testModulus2EOF() throws Exception {
    // Create input that results in modulus=2 at EOF (2 base64 chars)
    byte[] input = StringUtils.getBytesUtf8("AA");
    ByteArrayInputStream data = new ByteArrayInputStream(input);
    Base64InputStream stream = new Base64InputStream(data);
    
    byte[] decodedBytes = Base64TestData.streamToBytes(stream, new byte[1024]);
    assertNotNull("Decoded bytes should not be null", decodedBytes);
    assertTrue("Should decode at least one byte", decodedBytes.length > 0);
}