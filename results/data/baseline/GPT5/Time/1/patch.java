public int compareTo(DurationField durationField) {
        if (durationField == this) {
            return 0;
        }
        if (isSupported() && !durationField.isSupported()) {
            return 1;
        }
        if (!isSupported() && durationField.isSupported()) {
            return -1;
        }
        long thisMillis = getUnitMillis();
        long otherMillis = durationField.getUnitMillis();
        if (thisMillis == otherMillis) {
            return 0;
        }
        return thisMillis < otherMillis ? -1 : 1;
    }