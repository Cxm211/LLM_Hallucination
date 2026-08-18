// com/fasterxml/jackson/databind/deser/TestDateDeserialization.java
public void testDateUtilISO8601NoMillisNoTimezoneNonDefault() throws Exception
    {
        ObjectReader r = MAPPER.readerFor(Date.class);
        TimeZone tz = TimeZone.getTimeZone("GMT+3");
        Date date1 = r.with(tz)
                .readValue(quote("1970-01-01T00:00:00"));
        // Explicit timezone in input must be used, regardless of configured reader TZ
        Date date2 = r.with(TimeZone.getTimeZone("GMT-5"))
                .readValue(quote("1970-01-01T00:00:00+03:00"));
        assertEquals(date1, date2);
    }