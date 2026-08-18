// com/fasterxml/jackson/databind/ser/jdk/SqlDateSerializationTest.java
public void testSqlDateConfigOverrideShapeString() throws Exception {
    ObjectMapper mapper = newObjectMapper();
    mapper.configOverride(java.sql.Date.class)
        .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
    String json = mapper.writeValueAsString(java.sql.Date.valueOf("2020-01-01"));
    // Buggy will output a number (no quotes), fixed will output a quoted string.
    assertTrue("Expected quoted string, got: " + json, json.startsWith("\"") && json.endsWith("\""));
}
