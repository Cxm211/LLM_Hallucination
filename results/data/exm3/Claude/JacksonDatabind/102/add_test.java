// com/fasterxml/jackson/databind/ser/jdk/SqlDateSerializationTest.java
public void testSqlDateConfigOverrideWithLocale() throws Exception
{
    ObjectMapper mapper = newObjectMapper();
    mapper.configOverride(java.sql.Date.class)
        .setFormat(JsonFormat.Value.forPattern("yyyy+MM+dd").withLocale(Locale.US));
    assertEquals("\"1980+04+14\"",
        mapper.writeValueAsString(java.sql.Date.valueOf("1980-04-14")));
}