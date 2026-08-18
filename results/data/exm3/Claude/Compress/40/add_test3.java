// org/apache/commons/compress/utils/BitInputStreamTest.java
@Test
public void littleEndianSingleBitReads() throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {
            (byte)170 // 10101010
        });
    BitInputStream bin = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
    assertEquals(0, bin.readBits(1));
    assertEquals(1, bin.readBits(1));
    assertEquals(0, bin.readBits(1));
    assertEquals(1, bin.readBits(1));
    assertEquals(0, bin.readBits(1));
    assertEquals(1, bin.readBits(1));
    assertEquals(0, bin.readBits(1));
    assertEquals(1, bin.readBits(1));
}