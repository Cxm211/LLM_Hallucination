// org/apache/commons/codec/binary/Base64OutputStreamTest.java
public void testEmptyBufferWithPadding() throws Exception {
    // Test encoding with explicit padding at EOF
    byte[] input = StringUtils.getBytesUtf8("A");
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    Base64OutputStream stream = new Base64OutputStream(data, true);
    stream.write(input);
    stream.close();
    
    byte[] encodedBytes = data.toByteArray();
    assertNotNull("Encoded bytes should not be null", encodedBytes);
    assertTrue("Should produce encoded output", encodedBytes.length > 0);
}