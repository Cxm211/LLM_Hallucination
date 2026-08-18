// com/fasterxml/jackson/databind/ser/jdk/SqlDateSerializationTest.java
public void testSqlDateConfigOverrideShapeNumber() throws Exception {
    ObjectMapper mapper = newObjectMapper();
    mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    mapper.configOverride(java.sql.Date.class)
        .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER));
    String json = mapper.writeValueAsString(java.sql.Date.valueOf("2020-01-01"));
    // Buggy will output a quoted string, fixed will output a number (no quotes).
    assertFalse("Expected number without quotes, got: " + json, json.startsWith("\""));
}
