// org/joda/time/format/TestDateTimeFormatter.java
public void testParseInto_hourMinute_withZone() {
    DateTimeFormatter f = DateTimeFormat.forPattern("H m").withZone(DateTimeZone.forOffsetHours(5));
    MutableDateTime result = new MutableDateTime(2004, 1, 9, 12, 20, 30, 0, DateTimeZone.UTC);
    assertEquals(4, f.parseInto(result, "15 30", 0));
    assertEquals(new MutableDateTime(2004, 1, 9, 15, 30, 0, 0, DateTimeZone.forOffsetHours(5)), result);
}
