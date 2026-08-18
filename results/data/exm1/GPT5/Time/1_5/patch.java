public int compareTo(DurationField durationField) {
        if (durationField == null) {
            throw new NullPointerException("durationField");
        }
        long thisUnit = getUnitMillis();
        long otherUnit = durationField.getUnitMillis();
        if (thisUnit == otherUnit) {
            return 0;
        }
        return thisUnit < otherUnit ? -1 : 1;
    }