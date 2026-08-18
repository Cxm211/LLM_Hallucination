// com/fasterxml/jackson/databind/filter/IgnorePropertyOnDeserTest.java
public void testIgnoreGetterWithSetterNotIgnored() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_GETTERS_AS_SETTERS);
        Simple1595 des = mapper.readValue(aposToQuotes("{\"id\":456,\"name\":\"john\"}"), Simple1595.class);
        assertEquals(456, des.getId());
        assertEquals("john", des.getName());
    }