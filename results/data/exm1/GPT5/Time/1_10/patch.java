public int compareTo(DurationField durationField) {
        if (durationField == this) {
            return 0;
        }
        boolean thisSupported = isSupported();
        boolean otherSupported = (durationField != null && durationField.isSupported());
        if (thisSupported && !otherSupported) {
            return 1;
        }
        if (!thisSupported && otherSupported) {
            return -1;
        }
        if (!thisSupported && !otherSupported) {
            return 0;
        }
        long thisUnit = getUnitMillis();
        long otherUnit = durationField.getUnitMillis();
        if (thisUnit == otherUnit) {
            return 0;
        }
        return (thisUnit < otherUnit) ? -1 : 1;
    }