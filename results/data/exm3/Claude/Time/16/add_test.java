// org/joda/time/format/TestDateTimeFormatter.java
public void testParseInto_monthDay_withDefaultYear_nonLeapYear() {
    DateTimeFormatter f = DateTimeFormat.forPattern("M d").withDefaultYear(2001);
    MutableDateTime result = new MutableDateTime(2004, 1, 9, 12, 20, 30, 0, LONDON);
    assertEquals(4, f.parseInto(result, "2 28", 0));
    assertEquals(new MutableDateTime(2004, 2, 28, 12, 20, 30, 0, LONDON), result);
}