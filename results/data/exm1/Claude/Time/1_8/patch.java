public int compareTo(DurationField durationField) {
    if (isSupported() && !durationField.isSupported()) {
        return 1;
    }
    if (!isSupported() && durationField.isSupported()) {
        return -1;
    }
    return 0;
}