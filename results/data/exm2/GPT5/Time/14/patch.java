public int[] add(ReadablePartial partial, int fieldIndex, int[] values, int valueToAdd) {
        // overridden as superclass algorithm can't handle
        // 2004-02-29 + 48 months -> 2008-02-29 type dates
        if (valueToAdd == 0) {
            return values;
        }
            // month is largest field and being added to, such as month-day
        if (DateTimeUtils.isContiguous(partial)) {
            long instant = 0L;
            boolean hasYear = false;
            for (int i = 0, isize = partial.size(); i < isize; i++) {
                if (partial.getFieldType(i) == DateTimeFieldType.year()) {
                    hasYear = true;
                    break;
                }
            }
            if (!hasYear) {
                instant = iChronology.year().set(instant, 2000);
            }
            for (int i = 0, isize = partial.size(); i < isize; i++) {
                instant = partial.getFieldType(i).getField(iChronology).set(instant, values[i]);
            }
            instant = add(instant, valueToAdd);
            return iChronology.get(partial, instant);
        } else {
            return super.add(partial, fieldIndex, values, valueToAdd);
        }
    }