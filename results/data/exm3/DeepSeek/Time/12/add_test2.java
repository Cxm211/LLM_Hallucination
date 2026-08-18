// org/joda/time/TestLocalDate_Constructors.java
public void testFactory_fromCalendarFields_beforeYearZero_comprehensive() throws Exception {
    GregorianCalendar cal = new GregorianCalendar(2, 0, 1, 0, 0, 0);
    cal.set(Calendar.ERA, GregorianCalendar.BC);
    LocalDate expected = new LocalDate(-1, 1, 1);
    assertEquals(expected, LocalDate.fromCalendarFields(cal));

    cal = new GregorianCalendar(100, 11, 31, 0, 0, 0);
    cal.set(Calendar.ERA, GregorianCalendar.BC);
    expected = new LocalDate(-99, 12, 31);
    assertEquals(expected, LocalDate.fromCalendarFields(cal));
}
