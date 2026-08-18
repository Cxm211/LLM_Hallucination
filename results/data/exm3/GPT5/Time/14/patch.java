public int[] add(ReadablePartial partial, int fieldIndex, int[] values, int valueToAdd) {
        // overridden as superclass algorithm can't handle
        // 2004-02-29 + 48 months -> 2008-02-29 type dates
        if (valueToAdd == 0) {
            return values;
        }
        // month is largest field and being added to, such as month-day
        if (DateTimeUtils.isContiguous(partial)) {
            // Use a base instant in a leap year to allow setting Feb 29
            long instant = iChronology.getDateTimeMillis(2004, 6, 9, 12, 0, 0, 0);
            for (int i = 0, isize = partial.size(); i < isize; i++) {
                instant = partial.getFieldType(i).getField(iChronology).set(instant, values[i]);
            }
            instant = add(instant, valueToAdd);
            return iChronology.get(partial, instant);
        } else {
            return super.add(partial, fieldIndex, values, valueToAdd);
        }
    }