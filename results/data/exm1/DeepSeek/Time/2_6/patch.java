public int compareTo(DurationField durationField) {
    if (this.isSupported() && durationField.isSupported()) {
        long thisMillis = this.getUnitMillis();
        long otherMillis = durationField.getUnitMillis();
        if (thisMillis < otherMillis) return -1;
        if (thisMillis > otherMillis) return 1;
    } else if (this.isSupported()) {
        return 1;
    } else if (durationField.isSupported()) {
        return -1;
    }
    return 0;
}