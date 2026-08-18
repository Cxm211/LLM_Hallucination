// com/fasterxml/jackson/databind/jsontype/TestExternalId.java
public void testExternalIdWithMultiplePropertiesReordered() throws Exception
{
    final String CLASS = Payload928.class.getName();
    ObjectMapper mapper = new ObjectMapper();
    
    // Test with type id in the middle
    final String middleCase = "{\"payload\":{\"something\":\"test\"},\"class\":\"" + CLASS + "\",\"extra\":\"value\"}";
    Envelope928 envelope = mapper.readValue(middleCase, Envelope928.class);
    assertNotNull(envelope);
    assertEquals(Payload928.class, envelope._payload.getClass());
}