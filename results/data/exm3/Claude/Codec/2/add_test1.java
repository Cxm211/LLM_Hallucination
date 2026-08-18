// org/apache/commons/codec/binary/Base64OutputStreamTest.java
public void testBase64TwoBytesOutputStream() throws Exception {
        byte[] decoded = new byte[]{(byte) 'A', (byte) 'B'};
        byte[] encoded = "QUI=\r\n".getBytes();
        testByteByByte(encoded, decoded, 76, CRLF);
        testByChunk(encoded, decoded, 76, CRLF);
    }