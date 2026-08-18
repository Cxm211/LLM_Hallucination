public int compareTo(DurationField durationField) {
        if (durationField == null || !durationField.isSupported()) {
            return isSupported() ? 1 : 0;
        }
        if (!isSupported()) {
            return -1;
        }
        long otherMillis = durationField.getUnitMillis();
        long thisMillis = getUnitMillis();
        if (thisMillis == otherMillis) {
            return 0;
        }
        if (thisMillis < otherMillis) {
            return -1;
        } else {
            return 1;
        }
    }