// org/apache/commons/codec/binary/Base64InputStreamTest.java::testMarkSupported
@Test
public void testMarkSupported() throws Exception {
    InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
    Base64InputStream b64stream = new Base64InputStream(ins);
    assertTrue(b64stream.markSupported());
}