public int compareTo(DurationField durationField) {
    long thisMillis = this.getUnitMillis();
    long otherMillis = durationField.getUnitMillis();
    if (thisMillis == otherMillis) {
        return 0;
    }
    return (thisMillis < otherMillis ? -1 : 1);
}