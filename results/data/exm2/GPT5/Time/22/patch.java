protected BasePeriod(long duration) {
        this(duration, null, org.joda.time.chrono.ISOChronology.getInstanceUTC());
        // bug [3264409]
    }