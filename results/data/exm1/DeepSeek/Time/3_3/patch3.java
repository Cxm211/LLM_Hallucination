public void addMonths(final int months) {
    DateTimeField field = getChronology().months();
    int currentValue = field.get(getMillis());
    int newValue = field.add(currentValue, months);
    setMillis(field.set(getMillis(), newValue));
}