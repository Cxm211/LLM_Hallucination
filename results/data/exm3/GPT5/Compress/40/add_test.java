// org/apache/commons/compress/utils/BitInputStreamTest.java::bigEndianDirect63
@Test
public void bigEndianDirect63() throws Exception {
    byte[] data = new byte[] {
        87, // 0x57
        45, // 0x2D
        66, // 0x42
        15, // 0x0F
        90, // 0x5A
        29, // 0x1D
        88, // 0x58
        61, // 0x3D
        33, // 0x21
        74  // 0x4A
    };
    ByteArrayInputStream in = new ByteArrayInputStream(data);
    BitInputStream bin = new BitInputStream(in, ByteOrder.BIG_ENDIAN);

    // Expected: top 63 bits of the first 8 bytes
    long v = 0;
    for (int i = 0; i < 8; i++) {
        v = (v << 8) | (data[i] & 0xFFL);
    }
    long expected = v >>> 1; // top 63 bits

    assertEquals(expected, bin.readBits(63));

    // Read remaining 1 bit from the 8th byte and check next few bits to ensure cache is correct
    assertEquals((v & 1L), bin.readBits(1)); // last bit of first 8 bytes
    assertEquals((long)(data[8] & 0xFF), bin.readBits(8)); // next full byte
}