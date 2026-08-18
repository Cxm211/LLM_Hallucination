public int compareTo(DurationField durationField) {
        if (durationField == null) {
            throw new NullPointerException();
        }
        long thisMillis = this.getMillis();
        long otherMillis = durationField.getMillis();
        if (thisMillis == otherMillis) {
            return 0;
        } else if (thisMillis < otherMillis) {
            return -1;
        } else {
            return 1;
        }
    }