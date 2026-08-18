// com/fasterxml/jackson/databind/ser/TestConfig.java
public void testDateFormatConfigWithNullDateFormat() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    TimeZone tz1 = TimeZone.getTimeZone("America/New_York");
    
    mapper.setTimeZone(tz1);
    assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());
    
    mapper.setDateFormat(null);
    
    assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());
    assertEquals(tz1, mapper.getDeserializationConfig().getTimeZone());
}