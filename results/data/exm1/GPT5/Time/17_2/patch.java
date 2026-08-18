public long adjustOffset(long instant, boolean earlierOrLater) {
        // evaluate 3 hours before and after to detect a transition
        long before = instant - 3L * DateTimeConstants.MILLIS_PER_HOUR;
        long after = instant + 3L * DateTimeConstants.MILLIS_PER_HOUR;
        int offsetBefore = getOffset(before);
        int offsetAfter = getOffset(after);
        // If offsets are the same, no overlap to adjust
        if (offsetBefore == offsetAfter) {
            return instant;
        }
        // Work out range of instants that have duplicate local times
        long local = convertUTCToLocal(instant);
        // Use surrounding instants (UTC) to guide resolution
        return convertLocalToUTC(local, false, earlierOrLater ? after : before);
    }