// org/apache/commons/codec/binary/Base64Codec13Test.java
public void testLongInputNoLineBreaks() throws EncoderException {
    BinaryEncoder enc = new Base64();
    byte[] data = new byte[58];
    for (int i = 0; i < 58; i++) {
        data[i] = (byte) i;
    }
    byte[] encoded = enc.encode(data);
    for (byte b : encoded) {
        assertFalse(b == '\n' || b == '\r');
    }
    assertEquals(80, encoded.length);
}
