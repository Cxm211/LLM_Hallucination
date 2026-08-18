// org/apache/commons/compress/archivers/sevenz/SevenZFileTest.java
public void testCompressedHeaderWithSmallDictionarySize() throws Exception {
    // Test case with small dictionary size where signed/unsigned doesn't matter
    // This ensures the fix doesn't break normal small values
    byte[] properties = new byte[5];
    properties[0] = 0x5d;
    properties[1] = 0x00;
    properties[2] = 0x10; // dictSize = 0x1000 = 4096
    properties[3] = 0x00;
    properties[4] = 0x00;
    
    org.apache.commons.compress.archivers.sevenz.Coder coder = new org.apache.commons.compress.archivers.sevenz.Coder();
    coder.properties = properties;
    
    org.apache.commons.compress.archivers.sevenz.LZMADecoder decoder = new org.apache.commons.compress.archivers.sevenz.LZMADecoder();
    
    java.io.ByteArrayInputStream testInput = new java.io.ByteArrayInputStream(new byte[100]);
    try {
        java.io.InputStream result = decoder.decode(testInput, coder, null);
        assertNotNull(result);
    } catch (Exception e) {
        fail("Should handle small dictionary sizes correctly: " + e.getMessage());
    }
}