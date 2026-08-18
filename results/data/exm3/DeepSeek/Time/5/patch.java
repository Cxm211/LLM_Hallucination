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
            boolean yearsSupported = type.isSupported(DurationFieldType.YEARS);
            if (yearsSupported) {
                // normalize years and months
                long totalMonths = FieldUtils.safeAdd(FieldUtils.safeMultiply(years, 12), months);
                int normalizedYears = (int) (totalMonths / 12);
                int normalizedMonths = (int) (totalMonths % 12);
                if (normalizedYears != 0) {
                    result = result.withYears(normalizedYears);
                }
                if (normalizedMonths != 0) {
                    result = result.withMonths(normalizedMonths);
                }
            } else {
                // years not supported, combine into months
                long totalMonths = FieldUtils.safeAdd(FieldUtils.safeMultiply(years, 12), months);
                if (totalMonths != 0) {
                    result = result.withMonths((int) totalMonths);
                }
            }
        }
        return result;
    }