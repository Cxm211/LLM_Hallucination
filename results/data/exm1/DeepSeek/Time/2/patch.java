public int compareTo(DurationField durationField) {
    long thisMillis = this.getMillis(1L);
    long otherMillis = durationField.getMillis(1L);
    if (thisMillis < otherMillis) {
        return -1;
    } else if (thisMillis > otherMillis) {
        return 1;
    } else {
        return 0;
    }
}