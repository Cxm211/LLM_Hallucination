// org/apache/commons/codec/binary/Base64OutputStreamTest.java::testBase64EmptyOutputStream
public void testNoTrailingCRLF() throws Exception {
        byte[] decoded = "foo".getBytes("UTF-8");
        byte[] encoded = "Zm9v".getBytes("US-ASCII");
        testByteByByte(encoded, decoded, 76, CRLF);
        testByChunk(encoded, decoded, 76, CRLF);
    }