public void addWeeks(final int weeks) {
    DateTimeField field = getChronology().weeks();
    int currentValue = field.get(getMillis());
    int newValue = field.add(currentValue, weeks);
    setMillis(field.set(getMillis(), newValue));
}