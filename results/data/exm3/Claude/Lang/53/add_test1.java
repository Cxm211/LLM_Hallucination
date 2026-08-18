// org/apache/commons/lang/time/DateUtilsTest.java
public void testRoundLang346_Additional2() throws Exception
{
    TimeZone.setDefault(defaultZone);
    dateTimeParser.setTimeZone(defaultZone);
    Calendar testCalendar = Calendar.getInstance();
    testCalendar.set(2007, 6, 2, 8, 8, 49);
    testCalendar.set(Calendar.MILLISECOND, 499);
    Date date = testCalendar.getTime();
    assertEquals("Second Round Down with 499 Milli Seconds Failed",
                 dateTimeParser.parse("July 2, 2007 08:08:49.000"),
                 DateUtils.round(date, Calendar.SECOND));

    testCalendar.set(2007, 6, 2, 8, 29, 50);
    testCalendar.set(Calendar.MILLISECOND, 0);
    date = testCalendar.getTime();
    assertEquals("Minute Round Down at 29:50 Failed",
                 dateTimeParser.parse("July 2, 2007 08:30:00.000"),
                 DateUtils.round(date, Calendar.MINUTE));
}