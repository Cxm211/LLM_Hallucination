// org/apache/commons/codec/binary/Base64OutputStreamTest.java
public void testDecodePartialGroups() throws Exception {
    // Test modulus 2: two characters "QQ"
    byte[] input2 = StringUtils.getBytesUtf8("QQ");
    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
    Base64OutputStream stream2 = new Base64OutputStream(baos2, false);
    stream2.write(input2);
    stream2.close();
    byte[] decoded2 = baos2.toByteArray();
    assertEquals(1, decoded2.length);
    // Test modulus 3: three characters "QQQ"
    byte[] input3 = StringUtils.getBytesUtf8("QQQ");
    ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
    Base64OutputStream stream3 = new Base64OutputStream(baos3, false);
    stream3.write(input3);
    stream3.close();
    byte[] decoded3 = baos3.toByteArray();
    assertEquals(2, decoded3.length);
    // Test modulus 1: single character "Q"
    byte[] input1 = StringUtils.getBytesUtf8("Q");
    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
    Base64OutputStream stream1 = new Base64OutputStream(baos1, false);
    stream1.write(input1);
    stream1.close();
    byte[] decoded1 = baos1.toByteArray();
    assertEquals(0, decoded1.length);
}
