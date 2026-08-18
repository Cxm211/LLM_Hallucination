public int[] add(ReadablePartial partial, int fieldIndex, int[] values, int valueToAdd) {
        // overridden as superclass algorithm can't handle
        // 2004-02-29 + 48 months -> 2008-02-29 type dates
        if (valueToAdd == 0) {
            return values;
        }
        // month is largest field and being added to, such as month-day
        if (DateTimeUtils.isContiguous(partial)) {
            long instant = 0L;
            for (int i = 0, isize = partial.size(); i < isize; i++) {
                instant = partial.getFieldType(i).getField(iChronology).set(instant, values[i]);
            }
            long newInstant = add(instant, valueToAdd);
            // Prevent overflowing the range outside the partial (e.g., TimeOfDay crossing day boundary)
            DurationField range = partial.getFieldType(0).getField(iChronology).getRangeDurationField();
            if (range != null && range.getDifferenceAsLong(newInstant, instant) != 0) {
                throw new IllegalArgumentException("The addition exceeded the boundaries of the partial");
            }
            return iChronology.get(partial, newInstant);
        } else {
            return super.add(partial, fieldIndex, values, valueToAdd);
        }
    }