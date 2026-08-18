// com/fasterxml/jackson/databind/introspect/TestMixinMerging.java
public void testMultiLevelMixInOverride() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
        .disable(MapperFeature.AUTO_DETECT_FIELDS)
        .disable(MapperFeature.AUTO_DETECT_GETTERS)
        .disable(MapperFeature.AUTO_DETECT_IS_GETTERS)
        .disable(MapperFeature.INFER_PROPERTY_MUTATORS);
    SimpleModule module = new SimpleModule("Test");
    abstract class BaseMixin {
        @JsonProperty("baseName")
        public abstract String getName();
    }
    abstract class ChildMixin extends BaseMixin {
        @Override
        @JsonProperty("childName")
        public String getName() { return null; }
    }
    class MyClass {
        private String name = "test";
        public String getName() { return name; }
    }
    module.setMixInAnnotation(MyClass.class, ChildMixin.class);
    mapper.registerModule(module);
    assertEquals("{\"childName\":\"test\"}", mapper.writeValueAsString(new MyClass()));
}
