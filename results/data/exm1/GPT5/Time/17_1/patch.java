// buggy code
    public long adjustOffset(long instant, boolean earlierOrLater) {
        // a bit messy, but will work in all non-pathological cases
        
        // evaluate 3 hours before and after to work out if anything is happening
        long instantBefore = instant - 3 * DateTimeConstants.MILLIS_PER_HOUR;
        long instantAfter = instant + 3 * DateTimeConstants.MILLIS_PER_HOUR;
        int offsetBefore = getOffset(instantBefore);
        int offsetAfter = getOffset(instantAfter);
        if (offsetBefore <= offsetAfter) {
            return instant;  // not an overlap (less than is a gap, equal is normal case)
        }
        
        // work out range of instants that have duplicate local times
        long diff = offsetBefore - offsetAfter; // positive
        long local = instant + offsetBefore;
        long start = local - diff;
        long end = local + diff;
        
        // calculate result
        if (earlierOrLater) {
            // move to earlier offset if within the overlap window
            if (instant >= start && instant < end) {
                return instant - diff;
            }
        } else {
            // move to later offset if within the overlap window
            if (instant >= start && instant < end) {
                return instant + diff;
            }
        }
        return instant;
          // currently in later offset
          // currently in earlier offset
    }