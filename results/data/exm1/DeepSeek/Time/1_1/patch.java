public int compareTo(DurationField durationField) {
    if (durationField.isSupported() == false) {
        return 1;
    }
    long thisMillis = getUnitMillis();
    long otherMillis = durationField.getUnitMillis();
    if (thisMillis < otherMillis) {
        return -1;
    } else if (thisMillis > otherMillis) {
        return 1;
    } else {
        return 0;
    }
}