// org/joda/time/format/TestDateTimeFormatter.java
public void testParseInto_monthDay_nonLeapYear_feb28() {
    DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
    MutableDateTime result = new MutableDateTime(2003, 1, 1, 0, 0, 0, 0, NEWYORK);
    assertEquals(4, f.parseInto(result, "2 28", 0));
    assertEquals(new MutableDateTime(2003, 2, 28, 0, 0, 0, 0, NEWYORK), result);
}