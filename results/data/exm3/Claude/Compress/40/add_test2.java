// org/apache/commons/compress/utils/BitInputStreamTest.java
@Test
public void bigEndianMultipleReadsWithBoundary() throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {
            (byte)255, // 11111111
            (byte)170, // 10101010
            85         // 01010101
        });
    BitInputStream bin = new BitInputStream(in, ByteOrder.BIG_ENDIAN);
    assertEquals(255, bin.readBits(8));
    assertEquals(10, bin.readBits(4));
    assertEquals(10, bin.readBits(4));
    assertEquals(5, bin.readBits(4));
    assertEquals(5, bin.readBits(4));
}