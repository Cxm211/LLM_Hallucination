public int compareTo(DurationField durationField) {
    long thisMillis = getMillis(1);
    long otherMillis = durationField.getMillis(1);
    if (thisMillis == otherMillis) {
        return 0;
    }
    return (thisMillis < otherMillis) ? -1 : 1;
}