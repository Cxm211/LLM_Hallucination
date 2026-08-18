// org/apache/commons/lang3/time/DateUtilsTest.java
public void testIsSameLocalTime_HourDifference() {
    GregorianCalendar cal1 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    GregorianCalendar cal2 = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    cal1.set(2004, 6, 9, 1, 30, 0);
    cal1.set(Calendar.MILLISECOND, 0);
    cal2.set(2004, 6, 9, 13, 30, 0);
    cal2.set(Calendar.MILLISECOND, 0);
    assertFalse(DateUtils.isSameLocalTime(cal1, cal2));
}