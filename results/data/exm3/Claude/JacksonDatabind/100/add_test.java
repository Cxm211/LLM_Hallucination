// com/fasterxml/jackson/databind/node/TestConversions.java
public void testBase64WithDifferentVariants() throws Exception
{
    byte[] input = new byte[] { 0x3E, 0x3F, (byte)0xFB, (byte)0xFF };
    
    // Encode with MIME variant
    String encoded = Base64Variants.MIME.encode(input);
    TextNode n = new TextNode(encoded);
    
    // Try to decode with MODIFIED_FOR_URL variant (different padding/encoding)
    JsonParser p = new TreeTraversingParser(n);
    assertEquals(JsonToken.VALUE_STRING, p.nextToken());
    byte[] data = p.getBinaryValue(Base64Variants.MODIFIED_FOR_URL);
    
    // Should still work with the variant parameter
    assertNotNull(data);
    p.close();
}