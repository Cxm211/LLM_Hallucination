// org/joda/time/format/TestDateTimeFormatter.java
public void testParseInto_monthDay_withZoneChange() {
    DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
    MutableDateTime result = new MutableDateTime(2004, 6, 15, 12, 30, 45, 500, NEWYORK);
    assertEquals(4, f.parseInto(result, "3 10", 0));
    assertEquals(new MutableDateTime(2004, 3, 10, 12, 30, 45, 500, NEWYORK), result);
}