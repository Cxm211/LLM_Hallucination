protected BasePeriod(long duration) {
        this(duration, (PeriodType) null, (Chronology) null);
        // bug [3264409]
    }