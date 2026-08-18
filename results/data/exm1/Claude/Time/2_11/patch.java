public int compareTo(DurationField durationField) {
    if (durationField == null) {
        throw new NullPointerException();
    }
    long otherMillis = durationField.getUnitMillis();
    long thisMillis = getUnitMillis();
    if (thisMillis < otherMillis) {
        return -1;
    } else if (thisMillis > otherMillis) {
        return 1;
    } else {
        return 0;
    }
}