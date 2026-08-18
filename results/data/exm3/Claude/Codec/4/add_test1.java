// org/apache/commons/codec/binary/Base64Codec13Test.java
public void testBase64DefaultConstructorChunking() throws Exception {
    Base64 base64 = new Base64();
    byte[] input = new byte[150];
    Arrays.fill(input, (byte) 65);
    byte[] encoded = base64.encode(input);
    int lineBreakCount = 0;
    for (byte b : encoded) {
        if (b == '\r' || b == '\n') {
            lineBreakCount++;
        }
    }
    assertEquals("Default Base64() should not chunk/add line separators", 0, lineBreakCount);
}