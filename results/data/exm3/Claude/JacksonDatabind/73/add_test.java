// com/fasterxml/jackson/databind/deser/ReadOrWriteOnlyTest.java
public void testAutoAccessWithInferMutatorsDisabled() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.INFER_PROPERTY_MUTATORS);
        
        String json = mapper.writeValueAsString(new Pojo935());
        Pojo935 result = mapper.readValue(json, Pojo935.class);
        assertNotNull(result);
    }