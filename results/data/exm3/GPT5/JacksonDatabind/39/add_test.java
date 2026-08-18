// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java::testUnknownTypeIDRecoveryWithArray
public void testUnknownTypeIDRecoveryWithArray() throws Exception
    {
        ObjectReader reader = MAPPER.readerFor(CallRecord.class).without(
                DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        String json = aposToQuotes("{'application':'123','item':{'type':'xevent','location':['a','b']},'version':1.0}");
        CallRecord r = reader.readValue(json);
        assertNull(r.item);
        assertEquals("123", r.application);
    }