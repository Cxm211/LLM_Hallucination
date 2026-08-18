// com/fasterxml/jackson/databind/deser/TestDateDeserialization.java
public void testISO8601SingleDigitMilliseconds() throws Exception
{
    String inputStr;
    Date inputDate;
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    // Test single digit milliseconds
    inputStr = "2014-10-03T18:00:00.1+00:00";
    inputDate = MAPPER.readValue(quote(inputStr), java.util.Date.class);
    c.setTime(inputDate);
    assertEquals(2014, c.get(Calendar.YEAR));
    assertEquals(Calendar.OCTOBER, c.get(Calendar.MONTH));
    assertEquals(3, c.get(Calendar.DAY_OF_MONTH));
    assertEquals(18, c.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, c.get(Calendar.MINUTE));
    assertEquals(0, c.get(Calendar.SECOND));
    assertEquals(100, c.get(Calendar.MILLISECOND));
}