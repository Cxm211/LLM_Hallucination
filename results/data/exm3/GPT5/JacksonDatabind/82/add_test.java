// com/fasterxml/jackson/databind/filter/IgnorePropertyOnDeserTest.java::testIgnoreGetterViaMixinAllowsSetterDeser
public void testIgnoreGetterViaMixinAllowsSetterDeser() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.addMixIn(Simple1595.class, Simple1595GetterIgnoreMixin.class);
    Simple1595 des = mapper.readValue(aposToQuotes("{'id':1,'name':'joe'}"), Simple1595.class);
    assertEquals("joe", des.getName());
}

static abstract class Simple1595GetterIgnoreMixin {
    @com.fasterxml.jackson.annotation.JsonIgnore
    public abstract String getName();
}