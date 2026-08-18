public int compareTo(DurationField durationField) {
    if (durationField == null) {
        return 1;
    }
    if (!isSupported() && !durationField.isSupported()) {
        return 0;
    }
    if (!isSupported()) {
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
    if (thisMillis > otherMillis) {
        return 1;
    } else {
        return -1;
    }
}