// org/apache/commons/compress/compressors/bzip2/PythonTruncatedBzip2Test.java
@Test
public void testMultipleReadsAfterEndOfStream() throws IOException {
    final int length = TEXT.length();
    ByteBuffer buffer = ByteBuffer.allocate(length);
    bz2Channel.read(buffer);

    assertArrayEquals(Arrays.copyOfRange(TEXT.getBytes(), 0, length),
            buffer.array());

    buffer = ByteBuffer.allocate(1);
    try {
        bz2Channel.read(buffer);
        Assert.fail("First read after end should have thrown.");
    } catch (IOException e) {
        // pass
    }

    try {
        bz2Channel.read(buffer);
        Assert.fail("Second read after end should have thrown.");
    } catch (IOException e) {
        // pass
    }
}