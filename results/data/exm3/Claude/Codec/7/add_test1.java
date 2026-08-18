// org/apache/commons/codec/binary/Base64Test.java
public void testEncodeBase64StringWithSingleByte() throws Exception {
    byte[] singleByte = new byte[]{(byte) 0xFF};
    String encoded = Base64.encodeBase64String(singleByte);
    assertNotNull("encodeBase64String should not return null for single byte", encoded);
    assertEquals("Single byte encoding", "/w==", encoded);
}