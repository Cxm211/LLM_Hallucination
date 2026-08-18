public long adjustOffset(long instant, boolean earlierOrLater) {
    // evaluate 3 hours before and after to work out if anything is happening
    long instantBefore = convertUTCToLocal(instant - 3 * DateTimeConstants.MILLIS_PER_HOUR);
    long instantAfter = convertUTCToLocal(instant + 3 * DateTimeConstants.MILLIS_PER_HOUR);
    if (instantBefore == instantAfter) {
        return instant;  // not an overlap (less than is a gap, equal is normal case)
    }

    // work out range of instants that have duplicate local times
    long local = convertUTCToLocal(instant);
    // Compute the two possible UTC instants for the same local time.
    long utc1 = convertLocalToUTC(local, false, instant);
    // Use a far reference to get the other possible conversion.
    long utc2 = convertLocalToUTC(local, false, earlierOrLater ? utc1 - 24 * DateTimeConstants.MILLIS_PER_HOUR : utc1 + 24 * DateTimeConstants.MILLIS_PER_HOUR);
    // For an overlap, utc1 and utc2 differ. The earlier UTC corresponds to the later offset.
    if (earlierOrLater) {
        // Want later offset, so pick the earlier UTC instant.
        return Math.min(utc1, utc2);
    } else {
        // Want earlier offset, so pick the later UTC instant.
        return Math.max(utc1, utc2);
    }
}