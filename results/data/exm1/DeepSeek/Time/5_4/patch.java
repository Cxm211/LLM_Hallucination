public Period normalizedStandard(PeriodType type) {
    type = DateTimeUtils.getPeriodType(type);
    long millis = getMillis();  // no overflow can happen, even with Integer.MAX_VALUEs
    millis += (((long) getSeconds()) * ((long) DateTimeConstants.MILLIS_PER_SECOND));
    millis += (((long) getMinutes()) * ((long) DateTimeConstants.MILLIS_PER_MINUTE));
    millis += (((long) getHours()) * ((long) DateTimeConstants.MILLIS_PER_HOUR));
    millis += (((long) getDays()) * ((long) DateTimeConstants.MILLIS_PER_DAY));
    millis += (((long) getWeeks()) * ((long) DateTimeConstants.MILLIS_PER_WEEK));
    Period result = new Period(millis, type, ISOChronology.getInstanceUTC());
    int years = getYears();
    int months = getMonths();
    if (years != 0 || months != 0) {
        int yearsIndex = type.indexOf(DurationFieldType.years());
        int monthsIndex = type.indexOf(DurationFieldType.months());
        if (yearsIndex >= 0) {
            if (monthsIndex >= 0) {
                // both supported: normalize
                years = FieldUtils.safeAdd(years, months / 12);
                months = months % 12;
            } else {
                // years supported but months not: only keep years if months==0
                if (months != 0) {
                    throw new UnsupportedOperationException("Cannot convert months to years as months are not fixed duration");
                }
            }
        } else {
            if (monthsIndex >= 0) {
                // months supported but years not: convert years to months
                months = FieldUtils.safeAdd(FieldUtils.safeMultiply(years, 12), months);
                years = 0;
            } else {
                // neither supported
                throw new UnsupportedOperationException("Period type does not support years or months");
            }
        }
        if (years != 0) {
            result = result.withYears(years);
        }
        if (months != 0) {
            result = result.withMonths(months);
        }
    }
    return result;
}