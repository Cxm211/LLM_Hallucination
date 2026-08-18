public int compareTo(DurationField other) {
    long thisMillis = this.getUnitMillis();
    long otherMillis = other.getUnitMillis();
    if (thisMillis < otherMillis) {
        return -1;
    }
    if (thisMillis > otherMillis) {
        return 1;
    }
    return 0;
}