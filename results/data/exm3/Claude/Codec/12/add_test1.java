// org/apache/commons/codec/binary/Base64InputStreamTest.java
@Test
public void testMarkSupported() throws Throwable {
    InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B64));
    Base64InputStream b64stream = new Base64InputStream(ins);
    assertFalse(b64stream.markSupported());
}