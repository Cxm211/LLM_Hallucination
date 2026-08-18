public int compareTo(DurationField durationField) {
    if (durationField == null) {
        throw new NullPointerException("DurationField must not be null");
    }
    if (!durationField.isSupported()) {
        return 1;
    }
    if (!this.isSupported()) {
        return -1;
    }
    long thisMillis = this.getMillis(1);
    long otherMillis = durationField.getMillis(1);
    if (thisMillis == otherMillis) {
        return 0;
    } else if (thisMillis < otherMillis) {
        return -1;
    } else {
        return 1;
    }
}