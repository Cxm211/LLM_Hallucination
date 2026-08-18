// com/fasterxml/jackson/databind/node/TestConversions.java
public void testConversionOfPojosWithNullValue() throws Exception
{
    final Issue467Bean input = new Issue467Bean(0);
    final String EXP = "{\"x\":0}";
    
    String json = MAPPER.writeValueAsString(input);
    assertEquals(EXP, json);

    JsonNode tree = MAPPER.valueToTree(input);
    assertTrue("Expected Object, got "+tree.getNodeType(), tree.isObject());
    assertEquals(EXP, MAPPER.writeValueAsString(tree));
}