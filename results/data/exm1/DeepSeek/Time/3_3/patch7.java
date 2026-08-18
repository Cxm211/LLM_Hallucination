public void addMinutes(final int minutes) {
    DateTimeField field = getChronology().minutes();
    int currentValue = field.get(getMillis());
    int newValue = field.add(currentValue, minutes);
    setMillis(field.set(getMillis(), newValue));
}