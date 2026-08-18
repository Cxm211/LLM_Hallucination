// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java::testWithExplicitNullAsNullObject
public void testWithExplicitNullAsNullObject() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        AsPropertyWrapper wrapper = mapper.readValue("{ \"value\": null }", AsPropertyWrapper.class);
        assertNull(wrapper.value);
    }