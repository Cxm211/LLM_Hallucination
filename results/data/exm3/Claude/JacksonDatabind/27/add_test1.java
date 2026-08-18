// com/fasterxml/jackson/databind/jsontype/TestExternalId.java
public void testExternalIdAsLastProperty() throws Exception
{
    final String CLASS = Payload928.class.getName();
    ObjectMapper mapper = new ObjectMapper();
    
    // Test with type id at the end
    final String endCase = "{\"payload\":{\"something\":\"end\"},\"class\":\"" + CLASS + "\"}";
    Envelope928 envelope = mapper.readValue(endCase, Envelope928.class);
    assertNotNull(envelope);
    assertEquals(Payload928.class, envelope._payload.getClass());
    assertEquals("end", ((Payload928)envelope._payload).something);
}