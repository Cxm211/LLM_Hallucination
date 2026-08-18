public int[] add(ReadablePartial partial, int fieldIndex, int[] values, int valueToAdd) {
        // overridden as superclass algorithm can't handle
        // 2004-02-29 + 48 months -> 2008-02-29 type dates
        if (valueToAdd == 0) {
            return values;
        }
        // month is largest field and being added to, such as month-day
        if (DateTimeUtils.isContiguous(partial)) {
            long instant = 0L;
            // Use a safe baseline to allow setting any partial (e.g., Feb 29) without failure
            // Choose a leap year and a neutral date/time to avoid DST issues
            instant = DateTimeFieldType.year().getField(iChronology).set(instant, 2000);
            instant = DateTimeFieldType.monthOfYear().getField(iChronology).set(instant, 6);
            instant = DateTimeFieldType.dayOfMonth().getField(iChronology).set(instant, 1);
            instant = DateTimeFieldType.millisOfDay().getField(iChronology).set(instant, 0);

            for (int i = 0, isize = partial.size(); i < isize; i++) {
                instant = partial.getFieldType(i).getField(iChronology).set(instant, values[i]);
            }
            instant = add(instant, valueToAdd);
            return iChronology.get(partial, instant);
        } else {
            return super.add(partial, fieldIndex, values, valueToAdd);
        }
    }