public long adjustOffset(long instant, boolean earlierOrLater) {
        // a bit messy, but will work in all non-pathological cases
        
        // evaluate 3 hours before and after to work out if anything is happening
        long before = instant - 3 * DateTimeConstants.MILLIS_PER_HOUR;
        long after = instant + 3 * DateTimeConstants.MILLIS_PER_HOUR;
        long localBefore = convertUTCToLocal(before);
        long localAfter = convertUTCToLocal(after);
        if (localBefore == localAfter) {
            return instant;  // not an overlap (less than is a gap, equal is normal case)
        }
        
        // work out range of instants that have duplicate local times
        long local = convertUTCToLocal(instant);
        // Use the corresponding UTC instants (before/after), not the local times, when disambiguating
        return convertLocalToUTC(local, false, earlierOrLater ? after : before);
        
        // calculate result
          // currently in later offset
          // currently in earlier offset
    }