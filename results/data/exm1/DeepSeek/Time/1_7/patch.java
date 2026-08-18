    public int compareTo(DurationField durationField) {
        long thisMillis = getMillis();
        long otherMillis = durationField.getMillis();
        if (thisMillis == otherMillis) {
            return 0;
        }
        return (thisMillis < otherMillis) ? -1 : 1;
    }