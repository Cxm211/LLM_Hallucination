    public long adjustOffset(long instant, boolean earlierOrLater) {
        // a bit messy, but will work in all non-pathological cases
        
        // evaluate 3 hours before and after to work out if anything is happening
        long before = instant - 3 * DateTimeConstants.MILLIS_PER_HOUR;
        long after = instant + 3 * DateTimeConstants.MILLIS_PER_HOUR;
        long offsetBefore = convertUTCToLocal(before) - before;
        long offsetAfter = convertUTCToLocal(after) - after;
        if (offsetBefore == offsetAfter) {
            return instant;  // no transition within 6 hours
        }
        
        // work out range of instants that have duplicate local times
        long local = convertUTCToLocal(instant);
        long resultBefore = convertLocalToUTC(local, false, before);
        long resultAfter = convertLocalToUTC(local, false, after);
        if (resultBefore == resultAfter) {
            // gap
            return instant;
        }
        // overlap
        return earlierOrLater ? resultAfter : resultBefore;
    }