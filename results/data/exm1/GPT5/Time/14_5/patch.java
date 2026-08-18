public int[] add(ReadablePartial partial, int fieldIndex, int[] values, int valueToAdd) {
        // overridden as superclass algorithm can't handle
        // 2004-02-29 + 48 months -> 2008-02-29 type dates
        if (valueToAdd == 0) {
            return values;
        }
        // month is largest field and being added to, such as month-day
        boolean isMonthDay = false;
        if (partial.size() == 2) {
            DateTimeFieldType ft0 = partial.getFieldType(0);
            DateTimeFieldType ft1 = partial.getFieldType(1);
            isMonthDay = (ft0 == DateTimeFieldType.monthOfYear() && ft1 == DateTimeFieldType.dayOfMonth());
        }
        if (DateTimeUtils.isContiguous(partial) || isMonthDay) {
            long instant = 0L;
            for (int i = 0, isize = partial.size(); i < isize; i++) {
                instant = partial.getFieldType(i).getField(iChronology).set(instant, values[i]);
            }
            instant = add(instant, valueToAdd);
            return iChronology.get(partial, instant);
        } else {
            return super.add(partial, fieldIndex, values, valueToAdd);
        }
    }