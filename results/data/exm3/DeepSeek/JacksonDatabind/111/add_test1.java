// com/fasterxml/jackson/databind/deser/jdk/JDKAtomicTypesDeserTest.java
public void testAtomicReferenceWithCustomNull() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Integer.class, new CustomIntegerDeserializer());
    mapper.registerModule(module);
    BeanCustomNull bean = mapper.readValue("{\"ref\": null}", BeanCustomNull.class);
    assertNotNull(bean.ref);
    assertEquals(Integer.valueOf(-1), bean.ref.get());
}

static class BeanCustomNull {
    public AtomicReference<Integer> ref;
}

static class CustomIntegerDeserializer extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return -1;
        }
        return p.getIntValue();
    }
    @Override
    public Integer getNullValue(DeserializationContext ctxt) {
        return -1;
    }
}
