// com/fasterxml/jackson/databind/node/TestConversions.java
public void testBase64EmptyData() throws Exception
{
    byte[] input = new byte[0];
    Base64Variant variant = Base64Variants.MIME_NO_LINEFEEDS;
    
    String encoded = variant.encode(input);
    TextNode n = new TextNode(encoded);
    
    JsonParser p = new TreeTraversingParser(n);
    assertEquals(JsonToken.VALUE_STRING, p.nextToken());
    byte[] data = p.getBinaryValue(variant);
    
    assertNotNull(data);
    assertEquals(0, data.length);
    p.close();
}