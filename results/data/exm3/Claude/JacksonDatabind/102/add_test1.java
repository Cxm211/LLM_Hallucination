// com/fasterxml/jackson/databind/ser/jdk/SqlDateSerializationTest.java
public void testSqlDateConfigOverrideWithTimeZone() throws Exception
{
    ObjectMapper mapper = newObjectMapper();
    mapper.configOverride(java.sql.Date.class)
        .setFormat(JsonFormat.Value.forPattern("yyyy+MM+dd").withTimeZone(TimeZone.getTimeZone("UTC")));
    assertEquals("\"1980+04+14\"",
        mapper.writeValueAsString(java.sql.Date.valueOf("1980-04-14")));
}