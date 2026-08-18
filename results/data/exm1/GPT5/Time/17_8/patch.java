public long adjustOffset(long instant, boolean earlierOrLater) {
        // evaluate 3 hours before and after to determine if an overlap occurs
        long beforeInstant = instant - 3 * DateTimeConstants.MILLIS_PER_HOUR;
        long afterInstant = instant + 3 * DateTimeConstants.MILLIS_PER_HOUR;

        // Determine offsets before and after. An overlap is when the offset decreases
        int offsetBefore = getOffset(beforeInstant);
        int offsetAfter = getOffset(afterInstant);
        if (offsetBefore <= offsetAfter) {
            // Not an overlap (normal or a gap) - no adjustment
            return instant;
        }

        // Overlap detected - choose earlier or later offset
        long local = convertUTCToLocal(instant);
        return convertLocalToUTC(local, false, earlierOrLater ? afterInstant : beforeInstant);
    }