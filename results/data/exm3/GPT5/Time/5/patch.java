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
            if (monthsSupported && !yearsSupported) {
                int totalMonths = FieldUtils.safeAdd(FieldUtils.safeMultiply(years, 12), months);
                if (totalMonths != 0) {
                    result = result.withMonths(totalMonths);
                }
            } else {
                years = FieldUtils.safeAdd(years, months / 12);
                months = months % 12;
                if (years != 0) {
                    if (!yearsSupported) {
                        throw new UnsupportedOperationException("PeriodType does not support years");
                    }
                    result = result.withYears(years);
                }
                if (months != 0) {
                    if (!monthsSupported) {
                        throw new UnsupportedOperationException("PeriodType does not support months");
                    }
                    result = result.withMonths(months);
                }
            }
        }
        return result;
    }