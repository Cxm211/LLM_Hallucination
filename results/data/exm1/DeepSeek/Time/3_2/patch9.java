public void addMillis(final int millis) {
    if (millis == 0) {
        return;
    }
    setMillis(getChronology().millis().add(getMillis(), millis));
}