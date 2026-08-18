// org/joda/time/format/TestDateTimeFormatter.java
public void testParseLocalDate_year_month_week_2015() {
    Chronology chrono = GJChronology.getInstanceUTC();
    DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-ww").withChronology(chrono);
    assertEquals(new LocalDate(2015, 1, 5, chrono), f.parseLocalDate("2015-01-01"));
}