// org/joda/time/TestDateTimeZoneCutover.java
public void test_getOffsetFromLocal_NewYork_Autumn() {
    DateTimeZone zone = DateTimeZone.forID("America/New_York");
    // Ambiguous time 1:30 should map to first occurrence (-04:00)
    doTest_getOffsetFromLocal(11, 4, 1, 30, "2007-11-04T01:30:00.000-04:00", zone);
    // After transition: 2:30 EST (-05:00)
    doTest_getOffsetFromLocal(11, 4, 2, 30, "2007-11-04T02:30:00.000-05:00", zone);
}
