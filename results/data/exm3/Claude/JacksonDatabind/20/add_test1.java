// com/fasterxml/jackson/databind/introspect/TestNamingStrategyStd.java
public void testNamingWithNullValueInObjectNode() throws Exception
{
    ObjectMapper m = new ObjectMapper();
    m.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
    ClassWithObjectNodeField result =
        m.readValue(
            "{ \"id\": \"3\", \"json\": { \"key\": null } }",
            ClassWithObjectNodeField.class);
    assertNotNull(result);
    assertEquals("3", result.id);
    assertNotNull(result.json);
    assertEquals(1, result.json.size());
    assertTrue(result.json.path("key").isNull());
}