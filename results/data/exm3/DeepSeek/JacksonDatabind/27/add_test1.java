// com/fasterxml/jackson/databind/jsontype/TestExternalId.java
public void testExternalIdWithAnySetter() throws Exception {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "class")
    static class Envelope {
        public Object payload;
        @JsonAnySetter
        public void any(String key, Object value) {}
    }
    static class Payload {
        public String something;
    }
    ObjectMapper mapper = new ObjectMapper();
    final String CLASS = Payload.class.getName();
    // external type id before payload with extra property handled by any setter
    String json = "{\"class\":\"" + CLASS + "\",\"payload\":{\"something\":\"test\"},\"extra\":\"value\"}";
    Envelope envelope = mapper.readValue(json, Envelope.class);
    assertNotNull(envelope);
    assertEquals(Payload.class, envelope.payload.getClass());
}
