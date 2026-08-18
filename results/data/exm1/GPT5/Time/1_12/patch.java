public int compareTo(DurationField durationField) {
        if (durationField == null) {
            throw new NullPointerException("DurationField must not be null");
        }
        boolean thisSupported = this.isSupported();
        boolean otherSupported = durationField.isSupported();
        if (thisSupported && !otherSupported) {
            return 1;
        }
        if (!thisSupported && otherSupported) {
            return -1;
        }
        if (!thisSupported && !otherSupported) {
            return 0;
        }
        long thisUnit = this.getUnitMillis();
        long otherUnit = durationField.getUnitMillis();
        if (thisUnit == otherUnit) {
            return 0;
        }
        return thisUnit < otherUnit ? -1 : 1;
    }