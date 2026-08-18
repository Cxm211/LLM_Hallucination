// org/joda/time/TestDateTimeZoneCutover.java
public void testAdjustOffset_Gap() {
    DateTimeZone zone = DateTimeZone.forID("America/New_York");
    DateTime dt = new DateTime(2011, 3, 13, 3, 0, zone);
    assertSame(dt, dt.withEarlierOffsetAtOverlap());
    assertSame(dt, dt.withLaterOffsetAtOverlap());
}
