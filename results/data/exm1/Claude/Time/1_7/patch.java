public int compareTo(DurationField durationField) {
        if (!isSupported()) {
            return 0;
        }
        if (!durationField.isSupported()) {
            return 1;
        }
        return 0;
    }