public int compareTo(DurationField durationField) {
    if (durationField == null || !durationField.isSupported()) {
        return 1;
    }
    return 0;
}