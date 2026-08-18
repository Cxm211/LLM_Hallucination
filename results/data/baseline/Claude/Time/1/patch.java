public int compareTo(DurationField durationField) {
        if (!isSupported()) {
            if (!durationField.isSupported()) {
                return 0;
            }
            return 1;
        }
        if (!durationField.isSupported()) {
            return -1;
        }
        return 0;
    }