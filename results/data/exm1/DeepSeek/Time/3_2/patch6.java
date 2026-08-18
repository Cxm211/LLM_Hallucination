public void addHours(final int hours) {
    if (hours == 0) {
        return;
    }
    setMillis(getChronology().hours().add(getMillis(), hours));
}