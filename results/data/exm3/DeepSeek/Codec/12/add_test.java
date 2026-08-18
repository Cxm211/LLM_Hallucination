// org/apache/commons/codec/binary/Base32InputStreamTest.java
@Test
    public void testMarkSupported() throws Exception {
        InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        Base32InputStream b32stream = new Base32InputStream(ins);
        assertTrue(b32stream.markSupported());
    }
