// org/joda/time/format/TestDateTimeFormatter.java
public void testParseInto_dayOnly_withDefaultYear() {
    DateTimeFormatter f = DateTimeFormat.forPattern("d").withDefaultYear(2000);
    MutableDateTime result = new MutableDateTime(2004, 3, 15, 12, 20, 30, 0, LONDON);
    assertEquals(2, f.parseInto(result, "25", 0));
    assertEquals(new MutableDateTime(2004, 3, 25, 12, 20, 30, 0, LONDON), result);
}