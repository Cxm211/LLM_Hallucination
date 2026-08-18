// ===== FIXED org.joda.time.base.BasePeriod :: BasePeriod [lines 78-85] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-22-fixed/src/main/java/org/joda/time/base/BasePeriod.java =====
    protected BasePeriod(int years, int months, int weeks, int days,
                         int hours, int minutes, int seconds, int millis,
                         PeriodType type) {
        super();
        type = checkPeriodType(type);
        iType = type;
        setPeriodInternal(years, months, weeks, days, hours, minutes, seconds, millis); // internal method
    }
