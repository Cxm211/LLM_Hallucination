public int compareTo(DurationField durationField) {
        if (durationField == null) {
            throw new NullPointerException("DurationField must not be null");
        }
        long thisUnit = getUnitMillis();
        long otherUnit = durationField.getUnitMillis();
        if (thisUnit < otherUnit) {
            return -1;
        } else if (thisUnit > otherUnit) {
            return 1;
        }
        return 0;
    }