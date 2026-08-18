public int compareTo(DurationField durationField) {
    if (durationField == null) {
        throw new NullPointerException("The duration field must not be null");
    }
    if (!isSupported() && !durationField.isSupported()) {
        return 0;
    }
    if (!isSupported()) {
        return 1;
    }
    if (!durationField.isSupported()) {
        return -1;
    }
    long thisMillis = getUnitMillis();
    long otherMillis = durationField.getUnitMillis();
    if (thisMillis > otherMillis) {
        return 1;
    } else if (thisMillis < otherMillis) {
        return -1;
    } else {
        return 0;
    }
}