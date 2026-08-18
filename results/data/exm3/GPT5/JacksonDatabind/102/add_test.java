// com/fasterxml/jackson/databind/ser/jdk/SqlDateSerializationTest.java::testSqlDateConfigOverrideDifferentPattern
public void testSqlDateConfigOverrideDifferentPattern() throws Exception
    {
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(java.sql.Date.class)
            .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd"));
        assertEquals("\"1999-12-31\"",
            mapper.writeValueAsString(java.sql.Date.valueOf("1999-12-31")));
    }