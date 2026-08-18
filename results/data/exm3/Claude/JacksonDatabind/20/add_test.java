// com/fasterxml/jackson/databind/introspect/TestNamingStrategyStd.java
public void testNamingWithEmptyObjectNode() throws Exception
{
    ObjectMapper m = new ObjectMapper();
    m.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
    ClassWithObjectNodeField result =
        m.readValue(
            "{ \"id\": \"2\", \"json\": { } }",
            ClassWithObjectNodeField.class);
    assertNotNull(result);
    assertEquals("2", result.id);
    assertNotNull(result.json);
    assertEquals(0, result.json.size());
}