// org/apache/commons/lang/time/DateUtilsTest.java
public void testRoundSecondWith500Milliseconds() throws Exception
{
    TimeZone.setDefault(defaultZone);
    dateTimeParser.setTimeZone(defaultZone);
    Calendar testCalendar = Calendar.getInstance();
    testCalendar.set(2007, 6, 2, 8, 8, 20);
    testCalendar.set(Calendar.MILLISECOND, 500);
    Date date = testCalendar.getTime();
    assertEquals("Second Round Up with 500 Milliseconds Failed",
                 dateTimeParser.parse("July 2, 2007 08:08:21.000"),
                 DateUtils.round(date, Calendar.SECOND));
}
