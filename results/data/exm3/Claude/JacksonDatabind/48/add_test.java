// com/fasterxml/jackson/databind/ser/TestFeatures.java
public void testVisibilityFeaturesGettersOnly() throws Exception
{
    ObjectMapper om = new ObjectMapper();
    om.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
    om.configure(MapperFeature.AUTO_DETECT_GETTERS, true);
    om.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
    om.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
    om.configure(MapperFeature.AUTO_DETECT_CREATORS, false);

    JavaType javaType = om.getTypeFactory().constructType(TClsWithGetter.class);
    BeanDescription desc = (BeanDescription) om.getSerializationConfig().introspect(javaType);
    List<BeanPropertyDefinition> props = desc.findProperties();
    if (props.size() < 1) {
        fail("Should find at least 1 property with getter enabled, found " + props.size());
    }
}

static class TClsWithGetter {
    private int value;
    public int getValue() { return value; }
}