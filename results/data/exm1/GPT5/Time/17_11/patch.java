public long adjustOffset(long instant, boolean earlierOrLater) {
        // evaluate 3 hours before and after to work out if anything is happening
        long threeHours = 3L * DateTimeConstants.MILLIS_PER_HOUR;
        long instantBefore = instant - threeHours;
        long instantAfter = instant + threeHours;

        int offsetBefore = getOffset(instantBefore);
        int offsetAfter = getOffset(instantAfter);
        if (offsetBefore <= offsetAfter) {
            return instant;  // not an overlap (gap or normal case)
        }

        // work out range of instants that have duplicate local times by resolving via reference instants
        long local = convertUTCToLocal(instant);
        return convertLocalToUTC(local, false, earlierOrLater ? instantAfter : instantBefore);
    }