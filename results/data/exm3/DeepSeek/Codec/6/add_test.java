// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testNonBase64Bytes() throws Exception {
    String nonBase64 = "   \n\n\t\t";
    String base64 = "YWJj";
    byte[] input = (nonBase64 + base64).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(input);
    org.apache.commons.codec.binary.Base64InputStream in = new org.apache.commons.codec.binary.Base64InputStream(bais);
    byte[] result = new byte[10];
    int c = in.read(result);
    assertTrue("Should read positive bytes", c > 0);
    assertEquals(3, c);
    assertEquals('a', result[0]);
    assertEquals('b', result[1]);
    assertEquals('c', result[2]);
    c = in.read(result);
    assertEquals(-1, c);
}
