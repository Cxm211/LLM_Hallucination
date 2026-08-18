protected static int between(ReadablePartial start, ReadablePartial end, ReadablePeriod zeroInstance) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("ReadablePartial objects must not be null");
        }
        if (start.size() != end.size()) {
            throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields");
        }
        boolean hasYear = false;
        for (int i = 0, isize = start.size(); i < isize; i++) {
            if (start.getFieldType(i) != end.getFieldType(i)) {
                throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields");
            }
            if (start.getFieldType(i) == DateTimeFieldType.year()) {
                hasYear = true;
            }
        }
        if (DateTimeUtils.isContiguous(start) == false) {
            throw new IllegalArgumentException("ReadablePartial objects must be contiguous");
        }
        Chronology chrono = DateTimeUtils.getChronology(start.getChronology()).withUTC();
        long base = 0L;
        if (!hasYear) {
            base = chrono.getDateTimeMillis(2004, 1, 1, 0);
        }
        int[] values = chrono.get(zeroInstance, chrono.set(start, base), chrono.set(end, base));
        return values[0];
    }