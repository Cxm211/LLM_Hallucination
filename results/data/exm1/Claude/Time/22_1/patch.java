protected BasePeriod(long duration) {
    this(duration, PeriodType.standard(), null);
    // bug [3264409]
}