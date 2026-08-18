// org/joda/time/TestDateTimeZoneCutover.java
public void test_DateTime_constructor_Moscow_Autumn_edge() {
    DateTime dt1 = new DateTime(2007, 10, 28, 2, 0, 0, 0, ZONE_MOSCOW);
    assertEquals("2007-10-28T02:00:00.000+04:00", dt1.toString());
    DateTime dt2 = new DateTime(2007, 10, 28, 2, 59, 59, 999, ZONE_MOSCOW);
    assertEquals("2007-10-28T02:59:59.999+04:00", dt2.toString());
}