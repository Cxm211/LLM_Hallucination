// com/fasterxml/jackson/databind/ser/jdk/SqlDateSerializationTest.java
public void testSqlDateConfigOverrideNumericShape() throws Exception
{
    ObjectMapper mapper = newObjectMapper();
    mapper.configOverride(java.sql.Date.class)
        .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER));
    long timestamp = java.sql.Date.valueOf("1980-04-14").getTime();
    assertEquals(String.valueOf(timestamp),
        mapper.writeValueAsString(java.sql.Date.valueOf("1980-04-14")));
}