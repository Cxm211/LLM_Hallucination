// com/fasterxml/jackson/databind/struct/TestUnwrapped.java
@Test
public void testRemovePropertyWithMultipleProps() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    
    // Test with all properties present
    String json = "{\"name\":\"Alice\", \"age\":25, \"location\":\"NYC\"}";
    Person p = mapper.readValue(json, Person.class);
    assertNotNull(p);
    
    // Test with partial properties (triggering property removal logic)
    json = "{\"NAME\":\"Bob\"}";
    p = mapper.readValue(json, Person.class);
    assertNotNull(p);
}