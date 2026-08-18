// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testZeroByteReadDecodeDoesNotHang() throws Exception {
        java.io.InputStream zero = new java.io.InputStream() {
            @Override
            public int read(byte[] b) { return 0; }
            @Override
            public int read() { return -1; }
        };
        Base64InputStream in = new Base64InputStream(zero, false, 0, null);
        assertEquals(-1, in.read());
    }