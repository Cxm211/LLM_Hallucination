// com/fasterxml/jackson/databind/introspect/TestMixinMerging.java
public void testMultiLevelMixinMerge() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
        .disable(MapperFeature.AUTO_DETECT_FIELDS)
        .disable(MapperFeature.AUTO_DETECT_GETTERS)
        .disable(MapperFeature.AUTO_DETECT_IS_GETTERS)
        .disable(MapperFeature.INFER_PROPERTY_MUTATORS);
    SimpleModule module = new SimpleModule("Test");
    module.setMixInAnnotation(Person.class, PersonMixin.class);
    mapper.registerModule(module);
    
    String json = mapper.writeValueAsString(new PersonImpl());
    assertTrue(json.contains("city"));
    assertTrue(json.contains("Seattle"));
}