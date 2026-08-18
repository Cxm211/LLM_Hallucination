public long adjustOffset(long instant, boolean earlierOrLater) {
        // Evaluate offsets 3 hours before and after to detect an overlap
        long instantBeforeUTC = instant - 3 * DateTimeConstants.MILLIS_PER_HOUR;
        long instantAfterUTC = instant + 3 * DateTimeConstants.MILLIS_PER_HOUR;
        int offsetBefore = getOffset(instantBeforeUTC);
        int offsetAfter = getOffset(instantAfterUTC);
        // If offsets are the same or increase, it's not an overlap (increase indicates a gap)
        if (offsetAfter >= offsetBefore) {
            return instant;
        }
        // Overlap detected; choose earlier or later offset based on flag
        long local = convertUTCToLocal(instant);
        return convertLocalToUTC(local, false, earlierOrLater ? instantBeforeUTC : instantAfterUTC);
    }