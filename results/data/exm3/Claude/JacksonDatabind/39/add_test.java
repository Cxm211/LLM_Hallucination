// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java
public void testUnknownTypeIDRecoveryWithNestedObject() throws Exception
{
    ObjectReader reader = MAPPER.readerFor(CallRecord.class).without(
            DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
    String json = aposToQuotes("{'item':{'type':'xevent','nested':{'deep':'value'}},'version':1.0,'application':'456','item2':{'type':'event','location':'location2'}}");
    CallRecord r = reader.readValue(json);
    assertNull(r.item);
    assertEquals("456", r.application);
    assertNotNull(r.item2);
}