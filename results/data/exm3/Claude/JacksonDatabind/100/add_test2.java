// com/fasterxml/jackson/databind/node/TestConversions.java
public void testBase64LargeData() throws Exception
{
    byte[] input = new byte[1024];
    for (int i = 0; i < input.length; ++i) {
        input[i] = (byte) (i % 256);
    }
    
    Base64Variant variant = Base64Variants.PEM;
    String encoded = variant.encode(input);
    TextNode n = new TextNode(encoded);
    
    JsonParser p = new TreeTraversingParser(n);
    assertEquals(JsonToken.VALUE_STRING, p.nextToken());
    byte[] data = p.getBinaryValue(variant);
    
    assertNotNull(data);
    assertArrayEquals(input, data);
    p.close();
}