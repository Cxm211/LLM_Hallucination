public int compareTo(DurationField durationField) {
    long thisMillis = getUnitMillis();
    long otherMillis = durationField.getUnitMillis();
    if (thisMillis == otherMillis) {
        return 0;
    } else if (thisMillis < otherMillis) {
        return -1;
    } else {
        return 1;
    }
}