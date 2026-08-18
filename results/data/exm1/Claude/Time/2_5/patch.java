public int compareTo(DurationField durationField) {
    if (durationField == null) {
        return 1;
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