// org/joda/time/TestDateTimeZoneCutover.java
public void testBug3476684_adjustOffset_gap() {
    final DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
    DateTime base = new DateTime(2012, 10, 20, 22, 15, zone);
    DateTime gapTime = base.plusHours(1);  // Should be in normal time, not a gap
    
    assertSame(gapTime, gapTime.withEarlierOffsetAtOverlap());
    assertSame(gapTime, gapTime.withLaterOffsetAtOverlap());
}