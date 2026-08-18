public int compareTo(DurationField durationField) {
    if (durationField == null) {
        return 1;
    }
    long otherMillis = durationField.getUnitMillis();
    long thisMillis = getUnitMillis();
    if (thisMillis == otherMillis) {
        return 0;
    }
    return (thisMillis < otherMillis) ? -1 : 1;
}