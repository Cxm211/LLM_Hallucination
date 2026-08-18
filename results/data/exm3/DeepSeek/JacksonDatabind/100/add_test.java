// com/fasterxml/jackson/databind/node/TestConversions.java
public void testBase64TextWithWrongVariantThrows() throws Exception {
    byte[] input = new byte[]{1, 2, 3};
    String encoded = Base64Variants.MIME.encode(input);
    TextNode n = new TextNode(encoded);
    JsonParser p = new TreeTraversingParser(n);
    assertEquals(JsonToken.VALUE_STRING, p.nextToken());
    try {
        p.getBinaryValue(Base64Variants.MODIFIED_FOR_URL);
        fail("Expected exception for mismatched variant");
    } catch (JsonParseException e) {
        // expected
    }
    p.close();
}
