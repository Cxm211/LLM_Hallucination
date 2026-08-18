// com/fasterxml/jackson/databind/ser/TestFeatures.java
public void testVisibilityFeaturesSetters() throws Exception
{
    ObjectMapper om = new ObjectMapper();
    om.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
    om.configure(MapperFeature.AUTO_DETECT_GETTERS, true);
    om.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, true);
    om.configure(MapperFeature.AUTO_DETECT_FIELDS, true);
    om.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
    om.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
    om.configure(MapperFeature.INFER_PROPERTY_MUTATORS, false);
    om.configure(MapperFeature.USE_ANNOTATIONS, true);
    JavaType javaType = om.getTypeFactory().constructType(SetterOnlyClass.class);
    BeanDescription desc = (BeanDescription) om.getSerializationConfig().introspect(javaType);
    List<BeanPropertyDefinition> props = desc.findProperties();
    if (props.size() != 0) {
        fail("Should find 0 properties, not "+props.size()+"; properties = "+props);
    }
}
static class SetterOnlyClass {
    private int value;
    public void setValue(int v) { value = v; }
}
