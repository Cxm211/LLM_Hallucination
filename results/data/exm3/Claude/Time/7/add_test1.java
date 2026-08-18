// org/joda/time/format/TestDateTimeFormatter.java
public void testParseInto_monthDay_leapYear_march() {
    DateTimeFormatter f = DateTimeFormat.forPattern("M d").withLocale(Locale.UK);
    MutableDateTime result = new MutableDateTime(2004, 3, 15, 18, 45, 30, 250, TOKYO);
    assertEquals(4, f.parseInto(result, "3 1", 0));
    assertEquals(new MutableDateTime(2004, 3, 1, 18, 45, 30, 250, TOKYO), result);
}