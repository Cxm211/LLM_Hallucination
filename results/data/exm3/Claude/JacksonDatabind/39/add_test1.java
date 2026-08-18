// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java
public void testUnknownTypeIDRecoveryWithArrayValue() throws Exception
{
    ObjectReader reader = MAPPER.readerFor(CallRecord.class).without(
            DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
    String json = aposToQuotes("{'item':{'type':'xevent','items':['a','b','c']},'version':2.0,'application':'789'}");
    CallRecord r = reader.readValue(json);
    assertNull(r.item);
    assertEquals("789", r.application);
}