// org/apache/commons/compress/compressors/bzip2/PythonTruncatedBzip2Test.java
@Test
public void testReadExactAmountOfData() throws IOException {
    final int length = TEXT.length();
    ByteBuffer buffer = ByteBuffer.allocate(length);
    int bytesRead = bz2Channel.read(buffer);

    assertEquals(length, bytesRead);
    assertArrayEquals(TEXT.getBytes(), buffer.array());

    buffer = ByteBuffer.allocate(10);
    try {
        bz2Channel.read(buffer);
        Assert.fail("Read beyond valid data should have thrown.");
    } catch (IOException e) {
        // pass
    }
}