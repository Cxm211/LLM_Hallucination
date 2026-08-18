// org/joda/time/format/TestDateTimeFormatter.java
public void testParseInto_monthDay_feb29_london_midday() {
    DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
    MutableDateTime result = new MutableDateTime(2004, 6, 1, 12, 0, 0, 0, DateTimeZone.forID("Europe/London"));
    assertEquals(4, f.parseInto(result, "2 29", 0));
    assertEquals(new MutableDateTime(2004, 2, 29, 12, 0, 0, 0, DateTimeZone.forID("Europe/London")), result);
}
