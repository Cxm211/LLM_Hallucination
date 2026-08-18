// org/joda/time/TestDateTimeZoneCutover.java
public void testBug3476684_adjustOffset_reverseOverlap() {
    final DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
    DateTime base = new DateTime(2012, 2, 25, 22, 15, zone);
    DateTime overlapFirst = base.plusHours(1);  // 23:15 (first)
    DateTime overlapSecond = base.plusHours(2);  // 23:15 (second)
    
    assertEquals(overlapSecond, overlapFirst.withLaterOffsetAtOverlap());
    assertEquals(overlapFirst, overlapSecond.withEarlierOffsetAtOverlap());
}