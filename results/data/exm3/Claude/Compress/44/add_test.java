// org/apache/commons/compress/utils/ChecksumCalculatingInputStreamTest.java
@Test(expected = NullPointerException.class)
public void testValidChecksumValidInputStreamDoesNotThrow() throws Exception {
    ChecksumCalculatingInputStream stream = new ChecksumCalculatingInputStream(new CRC32(), new ByteArrayInputStream(new byte[]{1, 2, 3}));
    assertEquals(0, stream.getValue());
    stream.read();
    assertTrue(stream.getValue() != 0);
}