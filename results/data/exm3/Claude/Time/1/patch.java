public int compareTo(DurationField durationField) {
    if (durationField == null) {
        return 1;
    }
    if (!isSupported()) {
        return durationField.isSupported() ? -1 : 0;
    }
    if (!durationField.isSupported()) {
        return 1;
    }
    long thisMillis = getUnitMillis();
    long otherMillis = durationField.getUnitMillis();
    if (thisMillis > otherMillis) {
        return 1;
    }
    if (thisMillis < otherMillis) {
        return -1;
    }
    return 0;
}