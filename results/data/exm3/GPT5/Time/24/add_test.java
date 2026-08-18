// org/joda/time/format/TestDateTimeFormatter.java::testParseLocalDate_month_week_weekyear_2010
public void testParseLocalDate_month_week_weekyear_2010() {
        Chronology chrono = GJChronology.getInstanceUTC();
        DateTimeFormatter f = DateTimeFormat.forPattern("MM-ww-xxxx").withChronology(chrono);
        assertEquals(new LocalDate(2010, 1, 4, chrono), f.parseLocalDate("01-01-2010"));
    }