// com/fasterxml/jackson/databind/deser/TestDateDeserialization.java
public void testISO8601MissingSecondsNoTimezone() throws Exception
    {
        String inputStr;
        Date inputDate;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    
        inputStr = "1997-07-16T19:20";
        inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
        c.setTime(inputDate);
        assertEquals(1997, c.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, c.get(Calendar.MONTH));
        assertEquals(16, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(19, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }
