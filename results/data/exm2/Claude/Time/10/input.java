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
        int[] values = chrono.get(zeroInstance, chrono.set(start, 0L), chrono.set(end, 0L));
        return values[0];
    }

// trigger testcase
public void testFactory_daysBetween_RPartial_MonthDay() {
        MonthDay start1 = new MonthDay(2, 1);
        MonthDay start2 = new MonthDay(2, 28);
        MonthDay end1 = new MonthDay(2, 28);
        MonthDay end2 = new MonthDay(2, 29);
        
        assertEquals(27, Days.daysBetween(start1, end1).getDays());
        assertEquals(28, Days.daysBetween(start1, end2).getDays());
        assertEquals(0, Days.daysBetween(start2, end1).getDays());
        assertEquals(1, Days.daysBetween(start2, end2).getDays());
        
        assertEquals(-27, Days.daysBetween(end1, start1).getDays());
        assertEquals(-28, Days.daysBetween(end2, start1).getDays());
        assertEquals(0, Days.daysBetween(end1, start2).getDays());
        assertEquals(-1, Days.daysBetween(end2, start2).getDays());
    }

public void testFactory_monthsBetween_RPartial_MonthDay() {
        MonthDay start = new MonthDay(2, 1);
        MonthDay end1 = new MonthDay(2, 28);
        MonthDay end2 = new MonthDay(2, 29);
        MonthDay end3 = new MonthDay(3, 1);
        
        assertEquals(0, Months.monthsBetween(start, end1).getMonths());
        assertEquals(0, Months.monthsBetween(start, end2).getMonths());
        assertEquals(1, Months.monthsBetween(start, end3).getMonths());
        
        assertEquals(0, Months.monthsBetween(end1, start).getMonths());
        assertEquals(0, Months.monthsBetween(end2, start).getMonths());
        assertEquals(-1, Months.monthsBetween(end3, start).getMonths());
    }
