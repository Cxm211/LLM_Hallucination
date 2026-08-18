// buggy function
    public Base64() {
        this(false);
    }

// trigger testcase
// org/apache/commons/codec/binary/Base64Codec13Test.java::testBinaryEncoder
public void testBinaryEncoder() throws EncoderException {
        BinaryEncoder enc = new Base64();
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                byte[] base64 = utf8(STRINGS[i]);
                byte[] binary = BYTES[i];
                boolean b = Arrays.equals(base64, enc.encode(binary));
                assertTrue("BinaryEncoder test-" + i, b);
            }
        }
    }

// org/apache/commons/codec/binary/Base64Codec13Test.java::testEncoder
public void testEncoder() throws EncoderException {
        Encoder enc = new Base64();
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                byte[] base64 = utf8(STRINGS[i]);
                byte[] binary = BYTES[i];
                boolean b = Arrays.equals(base64, (byte[]) enc.encode(binary));
                assertTrue("Encoder test-" + i, b);
            }
        }
    }
