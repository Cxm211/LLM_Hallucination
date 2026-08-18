public long adjustOffset(long instant, boolean earlierOrLater) {
        // evaluate 3 hours before and after to determine if there is an overlap
        long before = instant - 3 * DateTimeConstants.MILLIS_PER_HOUR;
        long after = instant + 3 * DateTimeConstants.MILLIS_PER_HOUR;

        long localBefore = convertUTCToLocal(before);
        long localAfter = convertUTCToLocal(after);
        long offsetBefore = localBefore - before;
        long offsetAfter = localAfter - after;

        // If the offset does not decrease, there is no overlap to adjust
        if (offsetBefore <= offsetAfter) {
            return instant;
        }

        // We are in the vicinity of an overlap; convert the local time back to UTC
        long local = convertUTCToLocal(instant);
        return convertLocalToUTC(local, false, earlierOrLater ? after : before);
    }