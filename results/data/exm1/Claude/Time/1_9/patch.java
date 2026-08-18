public int compareTo(DurationField durationField) {
        if (!isSupported()) {
            if (!durationField.isSupported()) {
                return 0;
            }
            return -1;
        }
        if (!durationField.isSupported()) {
            return 1;
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