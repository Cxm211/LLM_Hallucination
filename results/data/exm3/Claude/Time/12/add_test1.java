// org/joda/time/TestLocalDateTime_Constructors.java
public void testFactory_fromCalendarFields_AD() throws Exception {
    GregorianCalendar cal = new GregorianCalendar(2023, 5, 15, 10, 30, 45);
    cal.set(Calendar.ERA, GregorianCalendar.AD);
    cal.set(Calendar.MILLISECOND, 123);
    LocalDateTime expected = new LocalDateTime(2023, 6, 15, 10, 30, 45, 123);
    assertEquals(expected, LocalDateTime.fromCalendarFields(cal));
}