public long adjustOffset(long instant, boolean earlierOrLater) {
    long instantBefore = convertUTCToLocal(instant - 3 * DateTimeConstants.MILLIS_PER_HOUR);
    long instantAfter = convertUTCToLocal(instant + 3 * DateTimeConstants.MILLIS_PER_HOUR);
    if (instantBefore == instantAfter) {
        return instant;
    }
    
    long local = convertUTCToLocal(instant);
    long localBefore = convertUTCToLocal(instant - 3 * DateTimeConstants.MILLIS_PER_HOUR);
    long localAfter = convertUTCToLocal(instant + 3 * DateTimeConstants.MILLIS_PER_HOUR);
    
    if (localBefore < local && local < localAfter) {
        return instant;
    }
    
    return convertLocalToUTC(local, false, earlierOrLater ? instantAfter : instantBefore);
}