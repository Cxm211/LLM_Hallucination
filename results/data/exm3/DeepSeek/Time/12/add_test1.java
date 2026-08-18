// org/joda/time/TestLocalDateTime_Constructors.java
public void testFactory_fromDateFields_beforeYearZero_comprehensive() throws Exception {
    GregorianCalendar cal = new GregorianCalendar(2, 0, 1, 0, 0, 0);
    cal.set(Calendar.ERA, GregorianCalendar.BC);
    cal.set(Calendar.MILLISECOND, 0);
    LocalDateTime expected = new LocalDateTime(-1, 1, 1, 0, 0, 0, 0);
    assertEquals(expected, LocalDateTime.fromDateFields(cal.getTime()));

    cal = new GregorianCalendar(100, 11, 31, 23, 59, 59);
    cal.set(Calendar.ERA, GregorianCalendar.BC);
    cal.set(Calendar.MILLISECOND, 999);
    expected = new LocalDateTime(-99, 12, 31, 23, 59, 59, 999);
    assertEquals(expected, LocalDateTime.fromDateFields(cal.getTime()));
}
