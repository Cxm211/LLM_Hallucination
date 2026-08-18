public int compareTo(DurationField durationField) {
        if (this == durationField) {
            return 0;
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