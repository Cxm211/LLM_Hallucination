// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testEncodeEOF() throws Exception {
    byte[] data = "Hello World".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
    org.apache.commons.codec.binary.Base64InputStream in = new org.apache.commons.codec.binary.Base64InputStream(bais, true);
    byte[] result = new byte[8192];
    int c = in.read(result);
    assertTrue("First read should be positive", c > 0);
    c = in.read(result);
    assertEquals("Second read should be EOF", -1, c);
}
