    public int compareTo(DurationField durationField) {
        if (durationField == null) {
            return 1;
        }
        if (durationField.isSupported()) {
            return 1;
        }
        return 0;
    }