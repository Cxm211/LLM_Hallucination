// org/apache/commons/lang/time/DateUtilsTest.java
public void testRoundMinuteAtThirtyBoundary() throws Exception {
        TimeZone.setDefault(defaultZone);
        dateTimeParser.setTimeZone(defaultZone);
        Calendar testCalendar = Calendar.getInstance();
        testCalendar.set(2007, 6, 2, 8, 8, 30);
        testCalendar.set(Calendar.MILLISECOND, 0);
        Date date = testCalendar.getTime();
        assertEquals("Minute Round Up at 30 Seconds Failed",
                     dateTimeParser.parse("July 2, 2007 08:09:00.000"),
                     DateUtils.round(date, Calendar.MINUTE));
    }