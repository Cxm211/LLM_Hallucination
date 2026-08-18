// com/fasterxml/jackson/databind/node/TestConversions.java
public void testConversionOfPojosWithNegativeValue() throws Exception
{
    final Issue467Bean input = new Issue467Bean(-42);
    final String EXP = "{\"x\":-42}";
    
    String json = MAPPER.writeValueAsString(input);
    assertEquals(EXP, json);

    JsonNode tree = MAPPER.valueToTree(input);
    assertTrue("Expected Object, got "+tree.getNodeType(), tree.isObject());
    assertEquals(EXP, MAPPER.writeValueAsString(tree));
}