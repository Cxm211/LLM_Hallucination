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
            boolean yearsSupported = type.isSupported(DurationFieldType.years());
            boolean monthsSupported = type.isSupported(DurationFieldType.months());
            if (yearsSupported && monthsSupported) {
                years = FieldUtils.safeAdd(years, months / 12);
                months = months % 12;
                if (years != 0) {
                    result = result.withYears(years);
                }
                if (months != 0) {
                    result = result.withMonths(months);
                }
            } else if (monthsSupported) {
                int totalMonths = FieldUtils.safeAdd(FieldUtils.safeMultiply(years, 12), months);
                if (totalMonths != 0) {
                    result = result.withMonths(totalMonths);
                }
            } else if (yearsSupported) {
                if (months != 0) {
                    throw new UnsupportedOperationException("Cannot normalize to years as months is non-zero");
                }
                if (years != 0) {
                    result = result.withYears(years);
                }
            } else {
                // Neither years nor months supported
                throw new UnsupportedOperationException("PeriodType does not support years or months");
            }
        }
        return result;
    }