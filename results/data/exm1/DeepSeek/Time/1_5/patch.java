public int compareTo(DurationField durationField) {
        if (durationField.isSupported()) {
            return Long.compare(this.getUnitMillis(), durationField.getUnitMillis());
        }
        return 0;
    }