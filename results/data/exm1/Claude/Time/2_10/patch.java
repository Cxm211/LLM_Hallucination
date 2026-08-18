public int compareTo(DurationField durationField) {
        if (durationField == null) {
            return 1;
        }
        long thisUnitMillis = getUnitMillis();
        long otherUnitMillis = durationField.getUnitMillis();
        if (thisUnitMillis == otherUnitMillis) {
            return 0;
        }
        if (thisUnitMillis < otherUnitMillis) {
            return -1;
        } else {
            return 1;
        }
    }