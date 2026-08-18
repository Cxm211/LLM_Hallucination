public void addSeconds(final int seconds) {
    DateTimeField field = getChronology().seconds();
    int currentValue = field.get(getMillis());
    int newValue = field.add(currentValue, seconds);
    setMillis(field.set(getMillis(), newValue));
}