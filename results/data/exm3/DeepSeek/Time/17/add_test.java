// org/joda/time/TestDateTimeZoneCutover.java
public void testAdjustOffset_NormalNoTransition() {
    DateTimeZone zone = DateTimeZone.forID("UTC");
    DateTime dt = new DateTime(2012, 6, 1, 12, 0, zone);
    assertSame(dt, dt.withEarlierOffsetAtOverlap());
    assertSame(dt, dt.withLaterOffsetAtOverlap());
}
