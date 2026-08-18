// com/fasterxml/jackson/databind/node/TestObjectNode.java
public void testEmptyObjectWithNullValue() throws Exception
{
    ObjectNode object = MAPPER.createObjectNode();
    object.putNull("nullField");
    object.remove("nullField");

    String json = MAPPER.writeValueAsString(object);
    MyValue de = MAPPER.readValue(json, MyValue.class);
    assertNotNull(de);
    assertNotNull(de.value);
    assertTrue(de.value.isObject());
    assertEquals(0, de.value.size());
}