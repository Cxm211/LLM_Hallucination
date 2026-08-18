    public int compareTo(DurationField other) {
        long thisMillis = getUnitMillis();
        long otherMillis = other.getUnitMillis();
        if (thisMillis < otherMillis) {
            return -1;
        } else if (thisMillis > otherMillis) {
            return 1;
        } else {
            return 0;
        }
    }