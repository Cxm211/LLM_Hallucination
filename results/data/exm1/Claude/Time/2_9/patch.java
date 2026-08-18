public int compareTo(DurationField durationField) {
        if (durationField == null || !durationField.isSupported()) {
            if (!isSupported()) {
                return 0;
            }
            return 1;
        }
        if (!isSupported()) {
            return -1;
        }
        long thisMillis = getUnitMillis();
        long otherMillis = durationField.getUnitMillis();
        if (thisMillis == otherMillis) {
            return 0;
        }
        if (thisMillis < otherMillis) {
            return -1;
        }
        return 1;
    }