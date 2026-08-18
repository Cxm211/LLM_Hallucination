// ===== FIXED org.joda.time.base.BaseSingleFieldPeriod :: between(ReadablePartial, ReadablePartial, ReadablePeriod) [lines 89-107] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Time/Time-10-fixed/src/main/java/org/joda/time/base/BaseSingleFieldPeriod.java =====
    protected static int between(ReadablePartial start, ReadablePartial end, ReadablePeriod zeroInstance) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("ReadablePartial objects must not be null");
        }
        if (start.size() != end.size()) {
            throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields");
        }
        for (int i = 0, isize = start.size(); i < isize; i++) {
            if (start.getFieldType(i) != end.getFieldType(i)) {
                throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields");
            }
        }
        if (DateTimeUtils.isContiguous(start) == false) {
            throw new IllegalArgumentException("ReadablePartial objects must be contiguous");
        }
        Chronology chrono = DateTimeUtils.getChronology(start.getChronology()).withUTC();
        int[] values = chrono.get(zeroInstance, chrono.set(start, START_1972), chrono.set(end, START_1972));
        return values[0];
    }
