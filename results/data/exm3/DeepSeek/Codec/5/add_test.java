// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testDecodePartialGroups() throws Exception {
    // Test modulus 2: two characters "QQ"
    byte[] input2 = StringUtils.getBytesUtf8("QQ");
    ByteArrayInputStream bais2 = new ByteArrayInputStream(input2);
    Base64InputStream stream2 = new Base64InputStream(bais2);
    byte[] decoded2 = Base64TestData.streamToBytes(stream2, new byte[1024]);
    assertEquals(1, decoded2.length);
    // Test modulus 3: three characters "QQQ"
    byte[] input3 = StringUtils.getBytesUtf8("QQQ");
    ByteArrayInputStream bais3 = new ByteArrayInputStream(input3);
    Base64InputStream stream3 = new Base64InputStream(bais3);
    byte[] decoded3 = Base64TestData.streamToBytes(stream3, new byte[1024]);
    assertEquals(2, decoded3.length);
    // Test modulus 1: single character "Q"
    byte[] input1 = StringUtils.getBytesUtf8("Q");
    ByteArrayInputStream bais1 = new ByteArrayInputStream(input1);
    Base64InputStream stream1 = new Base64InputStream(bais1);
    byte[] decoded1 = Base64TestData.streamToBytes(stream1, new byte[1024]);
    assertEquals(0, decoded1.length);
}
