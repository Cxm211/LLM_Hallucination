// com/fasterxml/jackson/databind/ser/DateSerializationTest.java
public void testFormatWithLocale() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        mapper.setDateFormat(sdf);
        String json = mapper.writeValueAsString(new DateAsDefaultBeanWithTimezone(0L));
        assertTrue(json.contains("1970-01-01"));
    }