// org/apache/commons/codec/binary/Base64InputStreamTest.java
@Test
public void testReadWithOffsetAndLargerBuffer() throws IOException {
    byte[] data = "U29tZURhdGE=".getBytes("UTF-8");
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    Base64InputStream in = new Base64InputStream(bais, false);
    byte[] buffer = new byte[15];
    int bytesRead = in.read(buffer, 3, 8);
    assertTrue("Should read some bytes", bytesRead > 0);
    in.close();
}