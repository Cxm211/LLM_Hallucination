// com/fasterxml/jackson/databind/deser/TestDateDeserialization.java
public void testDateUtilISO8601NoTimezoneHalfHourOffset() throws Exception
{
    ObjectReader r = MAPPER.readerFor(Date.class);
    TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
    Date date = r.with(tz)
            .readValue(quote("1970-01-01T00:00:00.000"));
    Date date2 = r.with(TimeZone.getTimeZone("GMT"))
            .readValue(quote("1970-01-01T00:00:00.000+05:30"));
    assertEquals(date, date2);
}
