public int compareTo(DurationField durationField) {
        if (this == durationField) {
            return 0;
        }
        if (durationField == null) {
            throw new NullPointerException("DurationField must not be null");
        }
        boolean thisSupported = this.isSupported();
        boolean otherSupported = durationField.isSupported();
        if (thisSupported != otherSupported) {
            return thisSupported ? 1 : -1;
        }
        long thisUnit = getUnitMillis();
        long otherUnit = durationField.getUnitMillis();
        if (thisUnit == otherUnit) {
            return 0;
        }
        return thisUnit < otherUnit ? -1 : 1;
    }