// org/apache/commons/codec/binary/Base64Test.java
public void testEncodeBase64StringWithBinaryData() throws Exception {
    byte[] binaryData = new byte[]{0x00, 0x01, 0x02, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF};
    String encoded = Base64.encodeBase64String(binaryData);
    assertNotNull("encodeBase64String should not return null for binary data", encoded);
    assertEquals("Binary data encoding", "AAEC/f7/", encoded);
}