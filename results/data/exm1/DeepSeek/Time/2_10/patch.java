    public int compareTo(DurationField durationField) {
        long thisMillis = getMillis();
        long otherMillis = durationField.getMillis();
        if (thisMillis < otherMillis) {
            return -1;
        }
        if (thisMillis > otherMillis) {
            return 1;
        }
        return 0;
    }