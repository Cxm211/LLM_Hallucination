// org/apache/commons/codec/binary/Base64Codec13Test.java::testBinaryEncoder
public void testNoChunkingByDefault() throws EncoderException {
        Encoder enc = new Base64();
        byte[] binary = new byte[200];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) (i & 0xFF);
        }
        byte[] encoded = (byte[]) enc.encode(binary);
        for (byte b : encoded) {
            assertFalse("Found line separator in output", b == '\r' || b == '\n');
        }
    }