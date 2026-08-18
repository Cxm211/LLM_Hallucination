public void addMinutes(final int minutes) {
    if (minutes == 0) {
        return;
    }
    setMillis(getChronology().minutes().add(getMillis(), minutes));
}