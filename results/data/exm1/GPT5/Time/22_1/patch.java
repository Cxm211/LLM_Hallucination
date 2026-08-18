protected BasePeriod(long duration) {
        this(duration, PeriodType.time(), null);
        // bug [3264409]
    }