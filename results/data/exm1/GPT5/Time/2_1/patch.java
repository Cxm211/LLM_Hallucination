public int compareTo(DurationField durationField) {
        if (durationField == null) {
            throw new NullPointerException("DurationField must not be null");
        }
        boolean thisSupported = isSupported();
        boolean otherSupported = durationField.isSupported();
        if (thisSupported != otherSupported) {
            return thisSupported ? 1 : -1;
        }
        long thisUnit = getUnitMillis();
        long otherUnit = durationField.getUnitMillis();
        if (thisUnit == otherUnit) {
            return 0;
        }
        return (thisUnit < otherUnit) ? -1 : 1;
    }