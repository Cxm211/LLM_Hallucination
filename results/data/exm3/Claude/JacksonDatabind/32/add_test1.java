// com/fasterxml/jackson/databind/deser/TestUntypedDeserialization.java
public void testNestedUntyped989_AdditionalEmptyObjectInMap() throws IOException
{
    Untyped989 pojo;
    ObjectReader r = MAPPER.readerFor(Untyped989.class);

    pojo = r.readValue("{\"nested\":{}}");
    assertTrue(pojo.value instanceof Map);
    Map<?, ?> map = (Map<?, ?>) pojo.value;
    assertTrue(map.containsKey("nested"));
    assertTrue(map.get("nested") instanceof Map);
    assertTrue(((Map<?, ?>) map.get("nested")).isEmpty());
}