public Period normalizedStandard(PeriodType type) {
    type = DateTimeUtils.getPeriodType(type);
    long millis = getMillis();
    millis += (((long) getSeconds()) * ((long) DateTimeConstants.MILLIS_PER_SECOND));
    millis += (((long) getMinutes()) * ((long) DateTimeConstants.MILLIS_PER_MINUTE));
    millis += (((long) getHours()) * ((long) DateTimeConstants.MILLIS_PER_HOUR));
    millis += (((long) getDays()) * ((long) DateTimeConstants.MILLIS_PER_DAY));
    millis += (((long) getWeeks()) * ((long) DateTimeConstants.MILLIS_PER_WEEK));
    Period result = new Period(millis, type, ISOChronology.getInstanceUTC());
    int years = getYears();
    int months = getMonths();
    if (years != 0 || months != 0) {
        if (type.indexOf(DurationFieldType.years()) >= 0 || type.indexOf(DurationFieldType.months()) >= 0) {
            years = FieldUtils.safeAdd(years, months / 12);
            months = months % 12;
            if (years != 0) {
                if (type.indexOf(DurationFieldType.years()) < 0) {
                    throw new IllegalArgumentException("Period does not support years");
                }
                result = result.withYears(years);
            }
            if (months != 0) {
                if (type.indexOf(DurationFieldType.months()) < 0) {
                    throw new IllegalArgumentException("Period does not support months");
                }
                result = result.withMonths(months);
            }
        } else {
            throw new IllegalArgumentException("Period does not support years or months");
        }
    }
    return result;
}