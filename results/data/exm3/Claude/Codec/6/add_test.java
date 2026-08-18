// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testCodec101WithOffset() throws Exception {
    byte[] codec101 = StringUtils.getBytesUtf8(Base64TestData.CODEC_101_MULTIPLE_OF_3);
    ByteArrayInputStream bais = new ByteArrayInputStream(codec101);
    Base64InputStream in = new Base64InputStream(bais);
    byte[] result = new byte[8192];
    int c = in.read(result, 10, 100);
    assertTrue("Codec101WithOffset: First read successful [c=" + c + "]", c > 0);
    
    c = in.read(result, 10, 100);
    assertTrue("Codec101WithOffset: Second read should report end-of-stream [c=" + c + "]", c < 0);
}