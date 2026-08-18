// com/fasterxml/jackson/databind/deser/TestUntypedDeserialization.java
public void testUntypedEndObject989() throws IOException {
        JsonParser p = MAPPER.createParser("{}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // END_OBJECT
        Object result = MAPPER.readValue(p, Object.class);
        assertTrue(result instanceof Map);
        assertTrue(((Map) result).isEmpty());
    }
