// org/apache/commons/compress/archivers/tar/TarUtilsTest.java
public void testParseNameUnsignedByte() {
        // Test high byte without null
        byte[] data1 = {(byte)0xFF};
        assertEquals("\u00FF", TarUtils.parseName(data1, 0, 1));
        // Test high byte with null terminator
        byte[] data2 = {(byte)0xFF, 0, 0x41};
        assertEquals("\u00FF", TarUtils.parseName(data2, 0, 3));
        // Test multiple high bytes
        byte[] data3 = {(byte)0x80, (byte)0x81};
        assertEquals("\u0080\u0081", TarUtils.parseName(data3, 0, 2));
    }
