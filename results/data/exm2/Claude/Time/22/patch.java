protected BasePeriod(long duration) {
    this(duration, null, ISOChronology.getInstanceUTC());
    // bug [3264409]
}