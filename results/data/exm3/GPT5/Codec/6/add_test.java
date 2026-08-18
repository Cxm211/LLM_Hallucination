// org/apache/commons/codec/binary/Base64InputStreamTest.java::testCodec101Encode
public void testCodec101Encode() throws Exception {
        byte[] data = StringUtils.getBytesUtf8(Base64TestData.CODEC_101_MULTIPLE_OF_3);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        Base64InputStream in = new Base64InputStream(bais, true);
        byte[] result = new byte[8192];
        int c = in.read(result);
        assertTrue("Codec101Encode: First read successful [c=" + c + "]", c > 0);

        c = in.read(result);
        assertTrue("Codec101Encode: Second read should report end-of-stream [c=" + c + "]", c < 0);
    }