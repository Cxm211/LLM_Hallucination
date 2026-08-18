// org/apache/commons/compress/utils/BitInputStreamTest.java
@Test
public void littleEndianZeroBitsRead() throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {
            42 // 00101010
        });
    BitInputStream bin = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
    assertEquals(0, bin.readBits(0));
    assertEquals(42, bin.readBits(8));
}