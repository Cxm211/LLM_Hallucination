// org/apache/commons/codec/binary/Base64Codec13Test.java
public void testBase64ConstructorDefaultsToNoLineBreaks() throws Exception {
    Base64 base64 = new Base64();
    byte[] input = new byte[100];
    for (int i = 0; i < input.length; i++) {
        input[i] = (byte) i;
    }
    byte[] encoded = base64.encode(input);
    String encodedStr = new String(encoded, "UTF-8");
    assertFalse("Default Base64() should not add line breaks", encodedStr.contains("\r") || encodedStr.contains("\n"));
}