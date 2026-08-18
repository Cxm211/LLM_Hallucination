// org/joda/time/TestDateTimeZoneCutover.java
public void test_getOffsetFromLocal_Moscow_Autumn_boundary() {
    doTest_getOffsetFromLocal(10, 27, 23, 59, "2007-10-27T23:59:00.000+04:00", ZONE_MOSCOW);
    doTest_getOffsetFromLocal(10, 28, 2, 59, 59, 999, "2007-10-28T02:59:59.999+04:00", ZONE_MOSCOW);
    doTest_getOffsetFromLocal(10, 28, 3, 0, 0, 0, "2007-10-28T03:00:00.000+03:00", ZONE_MOSCOW);
}