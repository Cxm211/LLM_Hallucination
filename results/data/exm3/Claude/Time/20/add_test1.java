// org/joda/time/format/TestDateTimeFormatterBuilder.java
public void test_printParseZoneLongestPrefix() {
    DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneId();
    DateTimeFormatter f = bld.toFormatter();
    
    DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, DateTimeZone.forID("Europe/London"));
    assertEquals("2007-03-04 12:30 Europe/London", f.print(dt));
    assertEquals(dt, f.parseDateTime("2007-03-04 12:30 Europe/London"));
}