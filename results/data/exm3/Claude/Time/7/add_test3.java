// org/joda/time/format/TestDateTimeFormatter.java
public void testParseInto_monthDay_endOfYear_differentZone() {
    DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
    MutableDateTime result = new MutableDateTime(2004, 12, 31, 23, 0, 0, 0, DateTimeZone.forOffsetHours(-8));
    assertEquals(4, f.parseInto(result, "1 15", 0));
    assertEquals(new MutableDateTime(2004, 1, 15, 23, 0, 0, 0, DateTimeZone.forOffsetHours(-8)), result);
}