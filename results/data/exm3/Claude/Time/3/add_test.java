// org/joda/time/TestMutableDateTime_Adds.java
public void testAddWeekyears_int_dstOverlapWinter_addZero() {
    MutableDateTime test = new MutableDateTime(2011, 10, 30, 2, 30, 0, 0, DateTimeZone.forID("Europe/Berlin"));
    test.addHours(1);
    assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
    test.addWeekyears(0);
    assertEquals("2011-10-30T02:30:00.000+01:00", test.toString());
}