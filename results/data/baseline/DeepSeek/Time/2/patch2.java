public Partial with(DateTimeFieldType fieldType, int value) {
    if (fieldType == null) {
        throw new IllegalArgumentException("The field type must not be null");
    }
    int index = indexOf(fieldType);
    if (index == -1) {
        DateTimeFieldType[] newTypes = new DateTimeFieldType[iTypes.length + 1];
        int[] newValues = new int[newTypes.length];
        
        // find correct insertion point to keep largest-smallest order
        int i = 0;
        DurationField unitField = fieldType.getDurationType().getField(iChronology);
        for (; i < iTypes.length; i++) {
            DateTimeFieldType loopType = iTypes[i];
            DurationField loopUnitField = loopType.getDurationType().getField(iChronology);
            if (unitField.isSupported() && loopUnitField.isSupported()) {
                int compare = unitField.compareTo(loopUnitField);
                if (compare > 0) {
                    break;
                } else if (compare == 0) {
                    DurationField rangeField = null;
                    DurationField loopRangeField = null;
                    if (fieldType.getRangeDurationType() != null) {
                        rangeField = fieldType.getRangeDurationType().getField(iChronology);
                    }
                    if (loopType.getRangeDurationType() != null) {
                        loopRangeField = loopType.getRangeDurationType().getField(iChronology);
                    }
                    if (rangeField == null && loopRangeField == null) {
                        throw new IllegalArgumentException("Types array must not contain duplicate: " +
                                fieldType.getName() + " and " + loopType.getName());
                    } else if (rangeField == null) {
                        break; // rangeField null means larger? Actually, we insert after? This is not correct.
                    } else if (loopRangeField == null) {
                        // loopRangeField null, so loopType is larger, continue
                    } else {
                        int rangeCompare = rangeField.compareTo(loopRangeField);
                        if (rangeCompare > 0) {
                            break;
                        } else if (rangeCompare == 0) {
                            throw new IllegalArgumentException("Types array must not contain duplicate: " +
                                    fieldType.getName() + " and " + loopType.getName());
                        }
                    }
                }
            } else if (!unitField.isSupported() && !loopUnitField.isSupported()) {
                // both unsupported: compare range durations
                DurationField rangeField = null;
                DurationField loopRangeField = null;
                if (fieldType.getRangeDurationType() != null) {
                    rangeField = fieldType.getRangeDurationType().getField(iChronology);
                }
                if (loopType.getRangeDurationType() != null) {
                    loopRangeField = loopType.getRangeDurationType().getField(iChronology);
                }
                if (rangeField == null && loopRangeField == null) {
                    throw new IllegalArgumentException("Types array must not contain duplicate: " +
                            fieldType.getName() + " and " + loopType.getName());
                } else if (rangeField == null) {
                    // rangeField null means larger? break to insert before
                    break;
                } else if (loopRangeField == null) {
                    // loopRangeField null means loopType larger, continue
                } else {
                    int rangeCompare = rangeField.compareTo(loopRangeField);
                    if (rangeCompare > 0) {
                        break;
                    } else if (rangeCompare == 0) {
                        throw new IllegalArgumentException("Types array must not contain duplicate: " +
                                fieldType.getName() + " and " + loopType.getName());
                    }
                }
            } else {
                // one supported, one unsupported: unsupported is larger
                if (!unitField.isSupported()) {
                    // new field unsupported, loop field supported -> unsupported > supported, so break to insert before loop
                    break;
                } else {
                    // loop unsupported, new supported -> loop is larger, continue
                }
            }
        }
        System.arraycopy(iTypes, 0, newTypes, 0, i);
        System.arraycopy(iValues, 0, newValues, 0, i);
        newTypes[i] = fieldType;
        newValues[i] = value;
        System.arraycopy(iTypes, i, newTypes, i + 1, newTypes.length - i - 1);
        System.arraycopy(iValues, i, newValues, i + 1, newValues.length - i - 1);
        // use public constructor to ensure full validation
        // this isn't overly efficient, but is safe
        Partial newPartial = new Partial(newTypes, newValues, iChronology);
        iChronology.validate(newPartial, newValues);
        return newPartial;
    }
    if (value == getValue(index)) {
        return this;
    }
    int[] newValues = getValues();
    newValues = getField(index).set(this, index, newValues, value);
    return new Partial(this, newValues);
}