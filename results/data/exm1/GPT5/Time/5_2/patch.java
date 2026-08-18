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
            int normYears = FieldUtils.safeAdd(years, months / 12);
            int normMonths = months % 12;
            if (normYears != 0 && !type.isSupported(DurationFieldType.years())) {
                throw new IllegalArgumentException("Cannot normalize period with years as requested PeriodType does not support years");
            }
            if (normMonths != 0 && !type.isSupported(DurationFieldType.months())) {
                throw new IllegalArgumentException("Cannot normalize period with months as requested PeriodType does not support months");
            }
            if (normYears != 0) {
                result = result.withYears(normYears);
            }
            if (normMonths != 0) {
                result = result.withMonths(normMonths);
            }
        }
        return result;
    }