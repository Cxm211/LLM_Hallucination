// com/fasterxml/jackson/databind/jsontype/TestPolymorphicWithDefaultImpl.java
public void testWithEmptyStringAsNullObjectDisabled() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{ \"value\": \"\" }", AsPropertyWrapper.class);
            fail("Should have thrown exception");
        } catch (Exception e) {
            // Expected: missing type property
        }
    }