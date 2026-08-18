public void addMillis(final int millis) {
    DateTimeField field = getChronology().millis();
    int currentValue = field.get(getMillis());
    int newValue = field.add(currentValue, millis);
    setMillis(field.set(getMillis(), newValue));
}