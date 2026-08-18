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
            years = FieldUtils.safeAdd(years, months / 12);
            months = months % 12;
            if (years != 0) {
                result = result.withYears(years);
            }
            if (months != 0) {
                result = result.withMonths(months);
            }
        }
        return result;
    }

// trigger testcase
public void testNormalizedStandard_periodType_months1() {
        Period test = new Period(1, 15, 0, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.months());
        assertEquals(new Period(1, 15, 0, 0, 0, 0, 0, 0), test);
        assertEquals(new Period(0, 27, 0, 0, 0, 0, 0, 0, PeriodType.months()), result);
    }

public void testNormalizedStandard_periodType_months2() {
        Period test = new Period(-2, 15, 0, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard(PeriodType.months());
        assertEquals(new Period(-2, 15, 0, 0, 0, 0, 0, 0), test);
        assertEquals(new Period(0, -9, 0, 0, 0, 0, 0, 0, PeriodType.months()), result);
    }

public void testNormalizedStandard_periodType_monthsWeeks() {
        PeriodType type = PeriodType.forFields(new DurationFieldType[]{
                        DurationFieldType.months(),
                        DurationFieldType.weeks(),
                        DurationFieldType.days()});
        Period test = new Period(2, 4, 6, 0, 0, 0, 0, 0);
        Period result = test.normalizedStandard(type);
        assertEquals(new Period(2, 4, 6, 0, 0, 0, 0, 0), test);
        assertEquals(new Period(0, 28, 6, 0, 0, 0, 0, 0, type), result);
    }
