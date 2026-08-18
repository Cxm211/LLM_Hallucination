// org/apache/commons/codec/binary/Base64InputStreamTest.java
public void testSetInitialBufferWithOffsetDecode() throws IOException {
        byte[] encoded = "SGVsbG8=".getBytes("UTF-8");
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(encoded);
        org.apache.commons.codec.binary.Base64InputStream in = new org.apache.commons.codec.binary.Base64InputStream(bis, false);
        byte[] buffer = new byte[10];
        int read = in.read(buffer, 2, 5);
        org.junit.Assert.assertEquals(5, read);
        byte[] expected = "Hello".getBytes("UTF-8");
        for (int i = 0; i < 5; i++) {
            org.junit.Assert.assertEquals(expected[i], buffer[i+2]);
        }
    }
