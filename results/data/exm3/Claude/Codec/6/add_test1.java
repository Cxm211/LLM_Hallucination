// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testCodec101WithNonZeroOffsetAndFullLength() throws Exception {
    byte[] codec101 = StringUtils.getBytesUtf8(Base64TestData.CODEC_101_MULTIPLE_OF_3);
    ByteArrayInputStream bais = new ByteArrayInputStream(codec101);
    Base64InputStream in = new Base64InputStream(bais);
    byte[] result = new byte[8192];
    int c = in.read(result, 5, 8192);
    assertTrue("Codec101NonZeroOffset: First read should fail or return data [c=" + c + "]", c != 0);
}