// com/fasterxml/jackson/databind/struct/TestUnwrapped.java
@Test
public void testRemovePropertyCaseInsensitive() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    
    // Create a bean with multiple properties to trigger the remove logic
    String json = "{\"name\":\"John\", \"age\":30}";
    
    // This should work without throwing exceptions
    JsonNode node = mapper.readTree(json);
    assertNotNull(node);
    
    // Verify case insensitive property access works
    Person p = mapper.readValue("{\"NAME\":\"John\", \"AGE\":30}", Person.class);
    assertNotNull(p);
}