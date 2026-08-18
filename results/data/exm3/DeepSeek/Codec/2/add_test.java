// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testBase64ThreeBytesNoLineBreak() throws Exception {
        byte[] encoded = "Zm9v".getBytes();
        byte[] decoded = "foo".getBytes();
        testByteByByte(encoded, decoded, 76, CRLF);
        testByChunk(encoded, decoded, 76, CRLF);
    }
