// org/joda/time/format/TestDateTimeFormatter.java
public void testParseLocalDate_weekyear_month_week_2014() {
    Chronology chrono = GJChronology.getInstanceUTC();
    DateTimeFormatter f = DateTimeFormat.forPattern("xxxx-MM-ww").withChronology(chrono);
    assertEquals(new LocalDate(2014, 1, 6, chrono), f.parseLocalDate("2014-01-01"));
}
