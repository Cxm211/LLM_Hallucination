protected BasePeriod(long duration) {
    this(duration, ISOChronology.getInstanceUTC(), null);
    // bug [3264409]
}