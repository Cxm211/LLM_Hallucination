// org/joda/time/format/TestDateTimeFormatter.java
public void testParseLocalDate_weekyear_month_week_2015() {
    Chronology chrono = GJChronology.getInstanceUTC();
    DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
    assertEquals(new LocalDate(2014, 12, 29, chrono), f.parseLocalDate("2015-01-01"));
}