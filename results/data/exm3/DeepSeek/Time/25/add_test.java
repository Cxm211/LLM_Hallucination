// org/joda/time/TestDateTimeZoneCutover.java
public void test_getOffsetFromLocal_NewYork_Spring() {
    DateTimeZone zone = DateTimeZone.forID("America/New_York");
    // Before transition: 1:30 EST (-05:00)
    doTest_getOffsetFromLocal(3, 11, 1, 30, "2007-03-11T01:30:00.000-05:00", zone);
    // Invalid time 2:30 should map to 3:30 EDT (-04:00)
    doTest_getOffsetFromLocal(3, 11, 2, 30, "2007-03-11T03:30:00.000-04:00", zone);
    // After transition: 3:30 EDT (-04:00)
    doTest_getOffsetFromLocal(3, 11, 3, 30, "2007-03-11T03:30:00.000-04:00", zone);
}
