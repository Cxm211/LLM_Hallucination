    public long adjustOffset(long instant, boolean earlierOrLater) {
        // a bit messy, but will work in all non-pathological cases
        
        // evaluate 3 hours before and after to work out if anything is happening
        long instantBefore = convertUTCToLocal(instant - 3 * DateTimeConstants.MILLIS_PER_HOUR);
        long instantAfter = convertUTCToLocal(instant + 3 * DateTimeConstants.MILLIS_PER_HOUR);
        if (instantBefore == instantAfter) {
            return instant;  // not an overlap (less than is a gap, equal is normal case)
        }
        
        // work out range of instants that have duplicate local times
        long local = convertUTCToLocal(instant);
        return convertLocalToUTC(local, false, earlierOrLater ? instantAfter : instantBefore);
        
        // calculate result
          // currently in later offset
          // currently in earlier offset
    }

// trigger testcase
public void testBug3476684_adjustOffset() {
        final DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
        DateTime base = new DateTime(2012, 2, 25, 22, 15, zone);
        DateTime baseBefore = base.plusHours(1);  // 23:15 (first)
        DateTime baseAfter = base.plusHours(2);  // 23:15 (second)
        
        assertSame(base, base.withEarlierOffsetAtOverlap());
        assertSame(base, base.withLaterOffsetAtOverlap());
        
        assertSame(baseBefore, baseBefore.withEarlierOffsetAtOverlap());
        assertEquals(baseAfter, baseBefore.withLaterOffsetAtOverlap());
        
        assertSame(baseAfter, baseAfter.withLaterOffsetAtOverlap());
        assertEquals(baseBefore, baseAfter.withEarlierOffsetAtOverlap());
    }
