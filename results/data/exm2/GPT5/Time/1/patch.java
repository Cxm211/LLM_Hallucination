    public int compareTo(DurationField durationField) {
        if (durationField == null) {
            throw new NullPointerException("DurationField must not be null");
        }
        long other = durationField.getUnitMillis();
        long mine = getUnitMillis();
        if (mine < other) {
            return -1;
        } else if (mine > other) {
            return 1;
        }
        return 0;
    }