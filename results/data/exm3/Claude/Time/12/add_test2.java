// org/joda/time/TestLocalDate_Constructors.java
public void testFactory_fromCalendarFields_beforeYearZero10() throws Exception {
    GregorianCalendar cal = new GregorianCalendar(10, 11, 25, 0, 0, 0);
    cal.set(Calendar.ERA, GregorianCalendar.BC);
    LocalDate expected = new LocalDate(-9, 12, 25);
    assertEquals(expected, LocalDate.fromCalendarFields(cal));
}