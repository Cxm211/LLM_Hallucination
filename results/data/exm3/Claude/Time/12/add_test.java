// org/joda/time/TestLocalDate_Constructors.java
public void testFactory_fromCalendarFields_AD() throws Exception {
    GregorianCalendar cal = new GregorianCalendar(2023, 5, 15, 10, 30, 45);
    cal.set(Calendar.ERA, GregorianCalendar.AD);
    LocalDate expected = new LocalDate(2023, 6, 15);
    assertEquals(expected, LocalDate.fromCalendarFields(cal));
}