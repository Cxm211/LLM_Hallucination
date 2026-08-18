// org/joda/time/TestDateTimeZoneCutover.java::testBug3476684_adjustOffset
public void testAdjustOffsetOverlap_London() {
        final DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime base = new DateTime(2012, 10, 28, 0, 15, zone);
        DateTime baseBefore = base.plusHours(1);  // 01:15 (first)
        DateTime baseAfter = base.plusHours(2);   // 01:15 (second)

        assertSame(base, base.withEarlierOffsetAtOverlap());
        assertSame(base, base.withLaterOffsetAtOverlap());

        assertSame(baseBefore, baseBefore.withEarlierOffsetAtOverlap());
        assertEquals(baseAfter, baseBefore.withLaterOffsetAtOverlap());

        assertSame(baseAfter, baseAfter.withLaterOffsetAtOverlap());
        assertEquals(baseBefore, baseAfter.withEarlierOffsetAtOverlap());
    }