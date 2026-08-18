public long adjustOffset(long instant, boolean earlierOrLater) {
        // evaluate 3 hours before and after to work out if anything is happening
        long instantBefore = convertUTCToLocal(instant - 3 * DateTimeConstants.MILLIS_PER_HOUR);
        long instantAfter = convertUTCToLocal(instant + 3 * DateTimeConstants.MILLIS_PER_HOUR);
        if (instantBefore == instantAfter) {
            return instant;  // not an overlap (less than is a gap, equal is normal case)
        }
        // work out range of instants that have duplicate local times
        long local = convertUTCToLocal(instant);
        long utcEarly = convertLocalToUTC(local, false, instant);
        long utcLate = convertLocalToUTC(local, true, instant);
        if (earlierOrLater) {
            return utcLate;
        } else {
            return utcEarly;
        }
    }