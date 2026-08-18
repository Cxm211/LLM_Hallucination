// com/fasterxml/jackson/databind/ser/TestConfig.java
public void testDateFormatConfigWithSameDateFormat() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    TimeZone tz1 = TimeZone.getTimeZone("Europe/London");
    TimeZone tz2 = TimeZone.getTimeZone("Asia/Tokyo");
    
    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
    f.setTimeZone(tz2);
    
    mapper.setTimeZone(tz1);
    mapper.setDateFormat(f);
    
    assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());
    
    mapper.setDateFormat(f);
    
    assertEquals(tz1, mapper.getSerializationConfig().getTimeZone());
}