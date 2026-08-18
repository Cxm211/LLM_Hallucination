// com/fasterxml/jackson/databind/jsontype/ext/ExternalTypeIdTest.java
public void testWithAsValueAndDefaultTyping() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enableDefaultTyping();
    ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new AsValueThingy(999L));
    String json = mapper.writeValueAsString(input);
    assertNotNull(json);
    ExternalTypeWithNonPOJO result = mapper.readValue(json, ExternalTypeWithNonPOJO.class);
    assertNotNull(result);
    assertNotNull(result.value);
    assertEquals(AsValueThingy.class, result.value.getClass());
    assertEquals(999L, ((AsValueThingy) result.value).rawDate);
}
