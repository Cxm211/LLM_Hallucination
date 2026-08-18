public int compareTo(DurationField durationField) {
        if (durationField == null) {
            throw new NullPointerException("The duration field must not be null");
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