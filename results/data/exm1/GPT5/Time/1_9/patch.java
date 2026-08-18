public int compareTo(DurationField durationField) {
        if (durationField == null) {
            throw new NullPointerException("DurationField must not be null");
        }
        long thisMillis = getUnitMillis();
        long otherMillis = durationField.getUnitMillis();
        if (thisMillis == otherMillis) {
            return 0;
        }
        return thisMillis < otherMillis ? -1 : 1;
    }