public long adjustOffset(long instant, boolean earlierOrLater) {
        // evaluate 3 hours before and after to work out if anything is happening
        long instantBeforeUTC = instant - 3 * DateTimeConstants.MILLIS_PER_HOUR;
        long instantAfterUTC = instant + 3 * DateTimeConstants.MILLIS_PER_HOUR;
        long localBefore = convertUTCToLocal(instantBeforeUTC);
        long localAfter = convertUTCToLocal(instantAfterUTC);
        if (localBefore == localAfter) {
            return instant;  // not an overlap (less than is a gap, equal is normal case)
        }
        // work out range of instants that have duplicate local times
        long local = convertUTCToLocal(instant);
        return convertLocalToUTC(local, false, earlierOrLater ? instantAfterUTC : instantBeforeUTC);
    }