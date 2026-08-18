// org/apache/commons/codec/binary/Base64Test.java
public void testEncodeBase64NonChunkedLargeData() {
    byte[] data = new byte[75];
    byte[] nonChunked = Base64.encodeBase64(data);
    int nonChunkedLen = nonChunked.length;
    int maxResultSize = nonChunkedLen + 1;
    byte[] result = Base64.encodeBase64(data, false, false, maxResultSize);
    assertEquals(nonChunkedLen, result.length);
}
