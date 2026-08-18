public int compareTo(DurationField durationField) {
    long thisMillis = this.getMillis(1);
    long otherMillis = durationField.getMillis(1);
    if (thisMillis < otherMillis) {
        return -1;
    } else if (thisMillis > otherMillis) {
        return 1;
    } else {
        return 0;
    }
}