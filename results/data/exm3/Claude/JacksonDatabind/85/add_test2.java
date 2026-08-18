// com/fasterxml/jackson/databind/ser/DateSerializationTest.java
public void testFormatWithDefaultSettings() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("dd/MM/yyyy"));
        String json = mapper.writeValueAsString(new DateAsDefaultBeanWithTimezone(86400000L));
        assertTrue(json.contains("02/01/1970") || json.contains("01/01/1970"));
    }