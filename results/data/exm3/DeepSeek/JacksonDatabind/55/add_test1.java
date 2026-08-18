// com/fasterxml/jackson/databind/ser/TestEnumSerialization.java
public void testEnumsWithJsonPropertyAsKeyRawMap() throws Exception
    {
        Map input = new HashMap();
        input.put(EnumWithJsonProperty.A, "b");
        assertEquals("{\"aleph\":\"b\"}", MAPPER.writeValueAsString(input));
    }
