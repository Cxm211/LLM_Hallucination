protected BasePeriod(long duration) {
        this(duration, null, PeriodType.standard());
        // bug [3264409]
    }