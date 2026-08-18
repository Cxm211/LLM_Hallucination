// com/fasterxml/jackson/databind/deser/TestUntypedDeserialization.java
public void testNestedUntyped989_AdditionalEmptyArray() throws IOException
{
    Untyped989 pojo;
    ObjectReader r = MAPPER.readerFor(Untyped989.class);

    pojo = r.readValue("[[], {}]");
    assertTrue(pojo.value instanceof List);
    List<?> list = (List<?>) pojo.value;
    assertEquals(2, list.size());
    assertTrue(list.get(0) instanceof List);
    assertTrue(((List<?>) list.get(0)).isEmpty());
    assertTrue(list.get(1) instanceof Map);
}