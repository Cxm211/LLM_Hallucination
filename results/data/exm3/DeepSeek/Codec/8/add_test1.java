// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testSetInitialBufferWithOffsetEncode() throws IOException {
        byte[] data = "Hello".getBytes("UTF-8");
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(data);
        org.apache.commons.codec.binary.Base64InputStream in = new org.apache.commons.codec.binary.Base64InputStream(bis, true);
        byte[] buffer = new byte[20];
        int read = in.read(buffer, 3, 10);
        org.junit.Assert.assertEquals(8, read);
        byte[] expected = "SGVsbG8=".getBytes("UTF-8");
        for (int i = 0; i < 8; i++) {
            org.junit.Assert.assertEquals(expected[i], buffer[i+3]);
        }
    }
