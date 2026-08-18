// org/joda/time/TestLocalDateTime_Constructors.java
public void testFactory_fromCalendarFields_beforeYearZero10() throws Exception {
    GregorianCalendar cal = new GregorianCalendar(10, 11, 25, 23, 59, 59);
    cal.set(Calendar.ERA, GregorianCalendar.BC);
    cal.set(Calendar.MILLISECOND, 999);
    LocalDateTime expected = new LocalDateTime(-9, 12, 25, 23, 59, 59, 999);
    assertEquals(expected, LocalDateTime.fromCalendarFields(cal));
}