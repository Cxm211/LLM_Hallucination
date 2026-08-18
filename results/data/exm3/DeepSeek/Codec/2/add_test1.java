// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testBase64ThreeBytesWithLineBreak() throws Exception {
        byte[] encoded = "Zm9v\r\n".getBytes();
        byte[] decoded = "foo".getBytes();
        testByteByByte(encoded, decoded, 4, CRLF);
        testByChunk(encoded, decoded, 4, CRLF);
    }
