// com/fasterxml/jackson/databind/ser/DateSerializationTest.java
public void testFormatWithExplicitTimezone() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        String json = mapper.writeValueAsString(new DateAsDefaultBeanWithTimezone(0L));
        assertEquals(aposToQuotes("{'date':'1970-01-01T00:00:00'}"), json);
    }