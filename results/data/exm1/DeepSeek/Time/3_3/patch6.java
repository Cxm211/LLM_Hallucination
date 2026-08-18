public void addHours(final int hours) {
    DateTimeField field = getChronology().hours();
    int currentValue = field.get(getMillis());
    int newValue = field.add(currentValue, hours);
    setMillis(field.set(getMillis(), newValue));
}