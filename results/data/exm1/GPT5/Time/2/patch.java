public int compareTo(DurationField durationField) {
        if (durationField == this) {
            return 0;
        }
        long thisMillis = getUnitMillis();
        long otherMillis = durationField.getUnitMillis();
        if (thisMillis == otherMillis) {
            return 0;
        }
        return (thisMillis < otherMillis) ? -1 : 1;
    }