// com/fasterxml/jackson/databind/jsontype/ext/ExternalTypeIdTest.java
public void testWithAsValueEmptyArray() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new AsValueThingy(0L));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        assertEquals("{\"value\":0,\"type\":\"thingy\"}", json);

        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(AsValueThingy.class, result.value.getClass());
        assertEquals(0L, ((AsValueThingy) result.value).rawDate);
    }