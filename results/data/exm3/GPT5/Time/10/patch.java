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

        // Use a leap year base (e.g., 2004) if the partial does not contain a year field,
        // so that month-day combinations like Feb 29 are valid and computed correctly.
        boolean hasYearField = false;
        for (int i = 0, isize = start.size(); i < isize; i++) {
            DateTimeFieldType t = start.getFieldType(i);
            if (t == DateTimeFieldType.year() || t == DateTimeFieldType.yearOfEra()
                    || t == DateTimeFieldType.yearOfCentury() || t == DateTimeFieldType.weekyear()) {
                hasYearField = true;
                break;
            }
        }

        long baseInstant = 0L;
        if (!hasYearField) {
            baseInstant = chrono.year().set(0L, 2004);
        }
        long startMillis = chrono.set(start, baseInstant);
        long endMillis = chrono.set(end, baseInstant);

        int[] values = chrono.get(zeroInstance, startMillis, endMillis);
        return values[0];
    }