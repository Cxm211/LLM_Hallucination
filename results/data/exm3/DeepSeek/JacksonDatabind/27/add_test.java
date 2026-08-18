// com/fasterxml/jackson/databind/jsontype/TestExternalId.java
public void testExternalIdWithIgnorable() throws Exception {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "class")
    @JsonIgnoreProperties("ignore")
    static class Envelope {
        public Object payload;
    }
    static class Payload {
        public String something;
    }
    ObjectMapper mapper = new ObjectMapper();
    final String CLASS = Payload.class.getName();
    // external type id before payload with ignorable property
    String json = "{\"class\":\"" + CLASS + "\",\"payload\":{\"something\":\"test\"},\"ignore\":\"value\"}";
    Envelope envelope = mapper.readValue(json, Envelope.class);
    assertNotNull(envelope);
    assertEquals(Payload.class, envelope.payload.getClass());
}
