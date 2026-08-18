// com/fasterxml/jackson/databind/filter/IgnorePropertyOnDeserTest.java
public void testIgnoreGetterWithFieldSetter() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Simple1595WithField config = new Simple1595WithField();
        config.id = 789;
        config.name = "jane";
        String json = mapper.writeValueAsString(config);
        assertEquals(aposToQuotes("{\"id\":789}"), json);
        Simple1595WithField des = mapper.readValue(aposToQuotes("{\"id\":789,\"name\":\"jane\"}"), Simple1595WithField.class);
        assertEquals("jane", des.name);
    }