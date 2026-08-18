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
        long startMillis = chrono.set(start, 0L);
        long endMillis = chrono.set(end, startMillis);
        boolean startValid = true;
        for (int i = 0; i < start.size(); i++) {
            DateTimeFieldType fieldType = start.getFieldType(i);
            if (chrono.get(fieldType, startMillis) != start.getValue(i)) {
                startValid = false;
                break;
            }
        }
        boolean endValid = true;
        for (int i = 0; i < end.size(); i++) {
            DateTimeFieldType fieldType = end.getFieldType(i);
            if (chrono.get(fieldType, endMillis) != end.getValue(i)) {
                endValid = false;
                break;
            }
        }
        if (!startValid || !endValid) {
            int startYear = chrono.year().get(startMillis);
            for (int i = 0; i < 1000; i++) {
                int tryYear = startYear + i;
                long yearStart = chrono.getDateTimeMillis(tryYear, 1, 1, 0, 0, 0, 0);
                startMillis = chrono.set(start, yearStart);
                endMillis = chrono.set(end, startMillis);
                startValid = true;
                for (int j = 0; j < start.size(); j++) {
                    DateTimeFieldType fieldType = start.getFieldType(j);
                    if (chrono.get(fieldType, startMillis) != start.getValue(j)) {
                        startValid = false;
                        break;
                    }
                }
                endValid = true;
                for (int j = 0; j < end.size(); j++) {
                    DateTimeFieldType fieldType = end.getFieldType(j);
                    if (chrono.get(fieldType, endMillis) != end.getValue(j)) {
                        endValid = false;
                        break;
                    }
                }
                if (startValid && endValid) {
                    break;
                }
            }
        }
        int[] values = chrono.get(zeroInstance, startMillis, endMillis);
        return values[0];
    }