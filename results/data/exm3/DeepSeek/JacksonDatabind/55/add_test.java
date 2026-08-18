// com/fasterxml/jackson/databind/ser/TestEnumSerialization.java
public void testEnumsWithJsonPropertyAsKeyRawEnum() throws Exception
    {
        Map<Enum, String> input = new EnumMap<Enum, String>(EnumWithJsonProperty.class);
        input.put(EnumWithJsonProperty.A, "b");
        assertEquals("{\"aleph\":\"b\"}", MAPPER.writeValueAsString(input));
    }
