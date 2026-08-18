// org/apache/commons/codec/binary/Base64InputStreamTest.java
@Test
public void testReadWithBufferLargerThanLength() throws IOException {
    byte[] data = "SGVsbG8gV29ybGQ=".getBytes("UTF-8");
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    Base64InputStream in = new Base64InputStream(bais, false);
    byte[] buffer = new byte[20];
    int bytesRead = in.read(buffer, 0, 10);
    assertTrue("Should read some bytes", bytesRead > 0);
    in.close();
}