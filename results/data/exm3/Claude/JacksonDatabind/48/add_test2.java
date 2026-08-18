// com/fasterxml/jackson/databind/ser/TestFeatures.java
public void testVisibilityFeaturesCreatorsOnly() throws Exception
{
    ObjectMapper om = new ObjectMapper();
    om.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
    om.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
    om.configure(MapperFeature.AUTO_DETECT_SETTERS, false);
    om.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
    om.configure(MapperFeature.AUTO_DETECT_CREATORS, true);

    JavaType javaType = om.getTypeFactory().constructType(TClsWithCreator.class);
    BeanDescription desc = (BeanDescription) om.getDeserializationConfig().introspect(javaType);
    List<BeanPropertyDefinition> props = desc.findProperties();
    if (props.size() < 1) {
        fail("Should find at least 1 property with creator enabled, found " + props.size());
    }
}

static class TClsWithCreator {
    private int id;
    public TClsWithCreator(int id) { this.id = id; }
    public int getId() { return id; }
}