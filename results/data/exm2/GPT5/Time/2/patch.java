public int compareTo(DurationField durationField) {
    if (durationField == null) {
        throw new NullPointerException("DurationField must not be null");
    }
    if (this == durationField) {
        return 0;
    }
    boolean thisSup = this.isSupported();
    boolean otherSup = durationField.isSupported();
    if (thisSup != otherSup) {
        return thisSup ? 1 : -1;
    }
    long thisUnit = this.getUnitMillis();
    long otherUnit = durationField.getUnitMillis();
    if (thisUnit == otherUnit) {
        return 0;
    }
    return thisUnit < otherUnit ? -1 : 1;
}