public Timer(long durationMillis) {
    if (durationMillis < 0L) {
        throw new IllegalArgumentException("durationMillis must be >= 0");
    }
    this.durationMillis = durationMillis;
}