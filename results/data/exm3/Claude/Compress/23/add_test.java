// org/apache/commons/compress/archivers/sevenz/SevenZFileTest.java
public void testCompressedHeaderWithLargeDictionarySize() throws Exception {
    // Test case where dictionary size bytes have high bit set (negative when treated as signed)
    // This tests the fix for proper unsigned byte handling
    byte[] properties = new byte[5];
    properties[0] = 0x5d; // typical LZMA properties byte
    properties[1] = (byte) 0xFF; // 255 unsigned, -1 signed
    properties[2] = (byte) 0xFF; // 255 unsigned, -1 signed
    properties[3] = (byte) 0xFF; // 255 unsigned, -1 signed
    properties[4] = 0x00; // This creates dictSize = 0x00FFFFFF = 16777215
    
    // Create a mock coder with these properties
    org.apache.commons.compress.archivers.sevenz.Coder coder = new org.apache.commons.compress.archivers.sevenz.Coder();
    coder.properties = properties;
    
    // Create decoder
    org.apache.commons.compress.archivers.sevenz.LZMADecoder decoder = new org.apache.commons.compress.archivers.sevenz.LZMADecoder();
    
    // Test that decode doesn't throw exception and handles unsigned bytes correctly
    java.io.ByteArrayInputStream testInput = new java.io.ByteArrayInputStream(new byte[100]);
    try {
        java.io.InputStream result = decoder.decode(testInput, coder, null);
        assertNotNull(result);
    } catch (Exception e) {
        fail("Should handle large unsigned dictionary sizes correctly: " + e.getMessage());
    }
}