// org/apache/commons/lang/time/DateUtilsTest.java
public void testRoundLang346_Additional1() throws Exception
{
    TimeZone.setDefault(defaultZone);
    dateTimeParser.setTimeZone(defaultZone);
    Calendar testCalendar = Calendar.getInstance();
    testCalendar.set(2007, 6, 2, 8, 8, 50);
    testCalendar.set(Calendar.MILLISECOND, 500);
    Date date = testCalendar.getTime();
    assertEquals("Second Round Up with 500 Milli Seconds Failed",
                 dateTimeParser.parse("July 2, 2007 08:08:51.000"),
                 DateUtils.round(date, Calendar.SECOND));

    testCalendar.set(2007, 6, 2, 8, 30, 20);
    testCalendar.set(Calendar.MILLISECOND, 0);
    date = testCalendar.getTime();
    assertEquals("Minute Round Up at 30 Seconds Failed",
                 dateTimeParser.parse("July 2, 2007 08:30:00.000"),
                 DateUtils.round(date, Calendar.MINUTE));

    testCalendar.set(2007, 6, 2, 8, 29, 59);
    testCalendar.set(Calendar.MILLISECOND, 999);
    date = testCalendar.getTime();
    assertEquals("Minute Round Down at 29:59.999 Failed",
                 dateTimeParser.parse("July 2, 2007 08:30:00.000"),
                 DateUtils.round(date, Calendar.MINUTE));
}