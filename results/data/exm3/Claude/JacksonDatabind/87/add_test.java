// com/fasterxml/jackson/databind/deser/TestDateDeserialization.java
public void testDateUtilISO8601NoTimezoneCase11() throws Exception
{
    ObjectReader r = MAPPER.readerFor(Date.class);
    TimeZone tz = TimeZone.getTimeZone("GMT-3");
    Date date1 = r.with(tz)
            .readValue(quote("1970-01-01T00:00:00.00"));
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    c.setTime(date1);
    assertEquals(1970, c.get(Calendar.YEAR));
    assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
    assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
    assertEquals(3, c.get(Calendar.HOUR_OF_DAY));
}