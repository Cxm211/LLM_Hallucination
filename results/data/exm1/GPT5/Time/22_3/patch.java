protected BasePeriod(long duration) {
        this(duration, (PeriodType) null);
        // bug [3264409] fixed by delegating to the two-arg constructor
    }