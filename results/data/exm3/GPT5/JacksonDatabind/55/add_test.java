// com/fasterxml/jackson/databind/ser/TestEnumSerialization.java::testEnumsWithJsonPropertyAsKey
public void testEnumsWithJsonPropertyAsKeyInLinkedHashMap() throws Exception
    {
        java.util.LinkedHashMap<EnumWithJsonProperty,String> input = new java.util.LinkedHashMap<EnumWithJsonProperty,String>();
        input.put(EnumWithJsonProperty.A, "b");
        assertEquals("{\"aleph\":\"b\"}", MAPPER.writeValueAsString(input));
    }