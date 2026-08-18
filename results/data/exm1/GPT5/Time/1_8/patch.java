public int compareTo(DurationField durationField) {
        if (durationField == null) {
            throw new NullPointerException("DurationField must not be null");
        }
        if (isSupported() && !durationField.isSupported()) {
            return 1;
        }
        if (!isSupported() && durationField.isSupported()) {
            return -1;
        }
        long thisUnit = getUnitMillis();
        long otherUnit = durationField.getUnitMillis();
        if (thisUnit == otherUnit) {
            return 0;
        }
        return thisUnit < otherUnit ? -1 : 1;
    }