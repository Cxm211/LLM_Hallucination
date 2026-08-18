    public long adjustOffset(long instant, boolean earlierOrLater) {
        long local = convertUTCToLocal(instant);
        long instantEarly = convertLocalToUTC(local, false, Long.MIN_VALUE);
        long instantLate = convertLocalToUTC(local, false, Long.MAX_VALUE);
        if (instantEarly == instantLate) {
            return instant;
        }
        return earlierOrLater ? Math.min(instantEarly, instantLate) : Math.max(instantEarly, instantLate);
    }