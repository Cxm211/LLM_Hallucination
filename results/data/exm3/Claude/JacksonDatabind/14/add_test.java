// com/fasterxml/jackson/databind/convert/TestUpdateValue.java
public void testTreeToValueWithNullNode() throws IOException
{
    ObjectMapper mapper = new ObjectMapper();
    JsonNode nullNode = NullNode.instance;
    DataB result = mapper.treeToValue(nullNode, DataB.class);
    assertNull(result);
}