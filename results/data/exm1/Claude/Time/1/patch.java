public int compareTo(DurationField durationField) {
    if (!durationField.isSupported()) {
        return -1;
    }
    if (!this.isSupported()) {
        return 1;
    }
    long thisMillis = this.getUnitMillis();
    long otherMillis = durationField.getUnitMillis();
    if (thisMillis > otherMillis) {
        return -1;
    }
    if (thisMillis < otherMillis) {
        return 1;
    }
    return 0;
}