// com/fasterxml/jackson/databind/deser/TestDateDeserialization.java
public void testDateUtilISO8601NoTimezonePositiveOffset() throws Exception
{
    ObjectReader r = MAPPER.readerFor(Date.class);
    TimeZone tz = TimeZone.getTimeZone("GMT+3");
    Date date = r.with(tz)
            .readValue(quote("1970-01-01T00:00:00.000"));
    Date date2 = r.with(TimeZone.getTimeZone("GMT"))
            .readValue(quote("1970-01-01T00:00:00.000+03:00"));
    assertEquals(date, date2);
}
